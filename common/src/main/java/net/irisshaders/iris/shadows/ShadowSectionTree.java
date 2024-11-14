package net.irisshaders.iris.shadows;

import it.unimi.dsi.fastutil.longs.Long2ReferenceLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceSortedMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.caffeinemc.mods.sodium.client.render.chunk.occlusion.SectionTree;
import net.caffeinemc.mods.sodium.client.render.chunk.tree.BaseMultiForest;
import net.caffeinemc.mods.sodium.client.render.chunk.tree.TraversableForest;
import net.caffeinemc.mods.sodium.client.render.chunk.tree.TraversableTree;
import net.caffeinemc.mods.sodium.client.render.chunk.tree.Tree;
import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.minecraft.core.SectionPos;

import java.util.Comparator;

public class ShadowSectionTree {
	private final RemovableMultiForest tree;

	public ShadowSectionTree(float buildDistance) {
		this.tree = new RemovableMultiForest(buildDistance);
	}

	private interface RemovableForest extends TraversableForest {
		void remove(int x, int y, int z);
	}

	private static class RemovableMultiForest {
		private final Long2ReferenceSortedMap<RemovableTree> trees;
		private final ReferenceArrayList<RemovableTree> treeSortList = new ReferenceArrayList<>();
		private RemovableTree lastTree;

		public RemovableMultiForest(float buildDistance) {
			var forestDim = BaseMultiForest.forestDimFromBuildDistance(buildDistance) + 1;
			this.trees = new Long2ReferenceLinkedOpenHashMap<>(forestDim * forestDim * forestDim);
		}

		public void calculateReducedAndClean() {
			for (var tree : this.trees.values()) {
				tree.calculateReduced();
				if (tree.isEmpty()) {
					this.trees.remove(tree.getTreeKey());
					if (this.lastTree == tree) {
						this.lastTree = null;
					}
				}
			}
		}

		public void traverse(SectionTree.VisibleSectionVisitor visitor, Viewport viewport) {
			var transform = viewport.getTransform();
			var cameraSectionX = transform.intX >> 4;
			var cameraSectionY = transform.intY >> 4;
			var cameraSectionZ = transform.intZ >> 4;

			// sort the trees by distance from the camera by sorting a packed index array.
			this.treeSortList.clear();
			this.treeSortList.ensureCapacity(this.trees.size());
			this.treeSortList.addAll(this.trees.values());
			for (var tree : this.treeSortList) {
				tree.calculateSortKey(cameraSectionX, cameraSectionY, cameraSectionZ);
			}

			this.treeSortList.unstableSort(Comparator.comparingInt(RemovableTree::getSortKey));

			// traverse in sorted front-to-back order for correct render order
			for (var tree : this.treeSortList) {
				// disable distance test in traversal because we don't use it here
				tree.traverse(visitor, viewport, 0, 0);
			}
		}

		public void add(int x, int y, int z) {
			if (this.lastTree != null && this.lastTree.add(x, y, z)) {
				return;
			}

			// get the tree coordinate by dividing by 64
			var treeX = x >> 6;
			var treeY = y >> 6;
			var treeZ = z >> 6;

			var treeKey = SectionPos.asLong(treeX, treeY, treeZ);
			var tree = this.trees.get(treeKey);

			if (tree == null) {
				var treeOffsetX = treeX << 6;
				var treeOffsetY = treeY << 6;
				var treeOffsetZ = treeZ << 6;
				tree = new RemovableTree(treeOffsetX, treeOffsetY, treeOffsetZ);
				this.trees.put(treeKey, tree);
			}

			tree.add(x, y, z);
			this.lastTree = tree;
		}

		public void remove(int x, int y, int z) {
			if (this.lastTree != null && this.lastTree.remove(x, y, z)) {
				return;
			}

			// get the tree coordinate by dividing by 64
			var treeX = x >> 6;
			var treeY = y >> 6;
			var treeZ = z >> 6;

			var treeKey = SectionPos.asLong(treeX, treeY, treeZ);
			var tree = this.trees.get(treeKey);

			if (tree == null) {
				return;
			}

			tree.remove(x, y, z);

			this.lastTree = tree;
		}
	}

	private static class RemovableTree extends TraversableTree {
		private boolean reducedIsValid = true;
		private int sortKey;

		public RemovableTree(int offsetX, int offsetY, int offsetZ) {
			super(offsetX, offsetY, offsetZ);
		}

		public boolean remove(int x, int y, int z) {
			x -= this.offsetX;
			y -= this.offsetY;
			z -= this.offsetZ;
			if (Tree.isOutOfBounds(x, y, z)) {
				return false;
			}

			var bitIndex = Tree.interleave6x3(x, y, z);
			this.tree[bitIndex >> 6] &= ~(1L << (bitIndex & 0b111111));

			this.reducedIsValid = false;

			return true;
		}

		@Override
		public void calculateReduced() {
			if (!this.reducedIsValid) {
				super.calculateReduced();
			}
		}

		@Override
		public boolean add(int x, int y, int z) {
			var result = super.add(x, y, z);
			if (result) {
				this.reducedIsValid = false;
			}
			return result;
		}

		public boolean isEmpty() {
			return this.treeDoubleReduced == 0;
		}

		public long getTreeKey() {
			return SectionPos.asLong(this.offsetX, this.offsetY, this.offsetZ);
		}

		@Override
		public int getPresence(int i, int i1, int i2) {
			throw new UnsupportedOperationException("Not implemented");
		}

		public void calculateSortKey(int cameraSectionX, int cameraSectionY, int cameraSectionZ) {
			var deltaX = Math.abs(this.offsetX + 32 - cameraSectionX);
			var deltaY = Math.abs(this.offsetY + 32 - cameraSectionY);
			var deltaZ = Math.abs(this.offsetZ + 32 - cameraSectionZ);
			this.sortKey = deltaX + deltaY + deltaZ + 1;
		}

		public int getSortKey() {
			return this.sortKey;
		}
	}

	public void addSection(int x, int y, int z) {
		this.tree.add(x, y, z);
	}

	public void removeSection(int x, int y, int z) {
		this.tree.remove(x, y, z);
	}

	public void traverseVisible(SectionTree.VisibleSectionVisitor visitor, Viewport viewport) {
		this.tree.traverse(visitor, viewport);
	}

	public void prepareTreesForTraversal() {
		this.tree.calculateReducedAndClean();
	}
}
