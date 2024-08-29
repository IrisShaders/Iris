package net.irisshaders.iris.shadows;

import net.irisshaders.iris.uniforms.CapturedRenderingState;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ShadowCascade {
	// Function are derived from Vulkan examples from Sascha Willems, and licensed under the MIT License:
	// https://github.com/SaschaWillems/Vulkan/tree/master/examples/shadowmappingcascade, which are based on
	// https://johanmedestrom.wordpress.com/2016/03/18/opengl-cascaded-shadow-maps/
	public static Matrix4f[] updateCascadeShadows(Matrix4fc modelView, Matrix4fc projection) {
		Matrix4f[] cascades = new Matrix4f[ShadowRenderTargets.NUM_CASCADES];
		float cascadeSplitLambda = 0.95f;

		float[] cascadeSplits = new float[ShadowRenderTargets.NUM_CASCADES];

		float nearClip = 0.05f;
		float farClip = 256.0f;
		float clipRange = farClip - nearClip;

		float maxZ = nearClip + clipRange;

		float range = maxZ - nearClip;
		float ratio = maxZ / nearClip;

		// Calculate split depths based on view camera frustum
		// Based on method presented in https://developer.nvidia.com/gpugems/GPUGems3/gpugems3_ch10.html
		for (int i = 0; i < ShadowRenderTargets.NUM_CASCADES; i++) {
			float p = (i + 1) / (float) (ShadowRenderTargets.NUM_CASCADES);
			float log = (float) (nearClip * java.lang.Math.pow(ratio, p));
			float uniform = nearClip + range * p;
			float d = cascadeSplitLambda * (log - uniform) + uniform;
			cascadeSplits[i] = (d - nearClip) / clipRange;
		}

		// Calculate orthographic projection matrix for each cascade
		float lastSplitDist = 0.0f;
		for (int i = 0; i < ShadowRenderTargets.NUM_CASCADES; i++) {
			float splitDist = cascadeSplits[i];

			Vector3f[] frustumCorners = new Vector3f[]{
				new Vector3f(-1.0f, 1.0f, -1.0f),
				new Vector3f(1.0f, 1.0f, -1.0f),
				new Vector3f(1.0f, -1.0f, -1.0f),
				new Vector3f(-1.0f, -1.0f, -1.0f),
				new Vector3f(-1.0f, 1.0f, 1.0f),
				new Vector3f(1.0f, 1.0f, 1.0f),
				new Vector3f(1.0f, -1.0f, 1.0f),
				new Vector3f(-1.0f, -1.0f, 1.0f),
			};

			// Project frustum corners into world space
			Matrix4f matShadowProjection = ShadowMatrices.createOrthoMatrix(ShadowRenderer.halfPlaneLength, 0.05f, 256.0f);

			Matrix4f matModelViewProjectionInv = CapturedRenderingState.INSTANCE.getGbufferProjection().mul(CapturedRenderingState.INSTANCE.getGbufferModelView(), new Matrix4f()).invert();
			Matrix4f invCam = matShadowProjection.mul(modelView.mul(matModelViewProjectionInv, new Matrix4f()), new Matrix4f());
			for (int j = 0; j < 8; j++) {
				Vector4f invCorner = new Vector4f(frustumCorners[j], 1.0f).mul(invCam);
				frustumCorners[j] = new Vector3f(invCorner.x / invCorner.w, invCorner.y / invCorner.w, invCorner.z / invCorner.w);
			}

			for (int j = 0; j < 4; j++) {
				Vector3f dist = new Vector3f(frustumCorners[j + 4]).sub(frustumCorners[j]);
				frustumCorners[j + 4] = new Vector3f(frustumCorners[j]).add(new Vector3f(dist).mul(splitDist));
				frustumCorners[j] = new Vector3f(frustumCorners[j]).add(new Vector3f(dist).mul(lastSplitDist));
			}

			// Get frustum center
			Vector3f frustumCenter = new Vector3f(0.0f);
			for (int j = 0; j < 8; j++) {
				frustumCenter.add(frustumCorners[j]);
			}
			frustumCenter.div(8.0f);

			float radius = 0.0f;
			for (int j = 0; j < 8; j++) {
				float distance = (new Vector3f(frustumCorners[j]).sub(frustumCenter)).length();
				radius = java.lang.Math.max(radius, distance);
			}
			radius = (float) java.lang.Math.ceil(radius * 16.0f) / 16.0f;

			Vector3f maxExtents = new Vector3f(radius);
			Vector3f minExtents = new Vector3f(maxExtents).mul(-1);

			cascades[i] = new Matrix4f().ortho
				(minExtents.x, maxExtents.x, minExtents.y, maxExtents.y, 0.05f, maxExtents.z - minExtents.z, true);

			// Store split distance and matrix in cascade
			float splitDistance = (nearClip + splitDist * clipRange) * -1.0f;

			lastSplitDist = cascadeSplits[i];
		}

		return cascades;
	}





	/*
	// Renders the shadow map for all cascades, and performs VSM conversion if necessary.
// This uses CPU-driven shadow map setup and scene submission
	void MeshRenderer::RenderShadowMap(ID3D11DeviceContext* context, final Camera& camera,
                                    final Matrix4f world, final Matrix4f characterWorld)
	{
		PIXEvent event(L"Mesh Shadow Map Rendering");
		ProfileBlock block(L"Shadow Map Rendering");

    final int ShadowMapSize = AppSettings::ShadowMapResolution();
    final float sMapSize = static_cast<float>(ShadowMapSize);

    final float MinDistance = AppSettings::AutoComputeDepthBounds ? reductionDepth.x
		: AppSettings::MinCascadeDistance;
    final float MaxDistance = AppSettings::AutoComputeDepthBounds ? reductionDepth.y
		: AppSettings::MaxCascadeDistance;

		// Compute the split distances based on the partitioning mode
		float CascadeSplits[4] = { 0.0f, 0.0f, 0.0f, 0.0f };

		if(AppSettings::PartitionMode == PartitionMode::Manual)
		{
			CascadeSplits[0] = MinDistance + AppSettings::SplitDistance0 * MaxDistance;
			CascadeSplits[1] = MinDistance + AppSettings::SplitDistance1 * MaxDistance;
			CascadeSplits[2] = MinDistance + AppSettings::SplitDistance2 * MaxDistance;
			CascadeSplits[3] = MinDistance + AppSettings::SplitDistance3 * MaxDistance;
		}
		else if(AppSettings::PartitionMode == PartitionMode::Logarithmic
			|| AppSettings::PartitionMode == PartitionMode::PSSM)
		{
			float lambda = 1.0f;
			if(AppSettings::PartitionMode == PartitionMode::PSSM)
				lambda = AppSettings::PSSMLambda;

			float nearClip = camera.NearClip();
			float farClip = camera.FarClip();
			float clipRange = farClip - nearClip;

			float minZ = nearClip + MinDistance * clipRange;
			float maxZ = nearClip + MaxDistance * clipRange;

			float range = maxZ - minZ;
			float ratio = maxZ / minZ;

			for(int i = 0; i < ShadowRenderTargets.NUM_CASCADES; ++i)
			{
				float p = (i + 1) / (float) ShadowRenderTargets.NUM_CASCADES;
				float log = (float) (minZ * Math.pow(ratio, p));
				float uniform = minZ + range * p;
				float d = lambda * (log - uniform) + uniform;
				CascadeSplits[i] = (d - nearClip) / clipRange;
			}
		}

		Matrix4f globalShadowMatrix = MakeGlobalShadowMatrix(camera);
		meshPSConstants.Data.ShadowMatrix = Matrix4f::Transpose(globalShadowMatrix);

		// Render the meshes to each cascade
		for(int cascadeIdx = 0; cascadeIdx < ShadowRenderTargets.NUM_CASCADES; ++cascadeIdx)
		{

			// Get the 8 points of the view frustum in world space
			Vector3f[] frustumCornersWS = new Vector3f[]
			{
				new Vector3f(-1.0f,  1.0f, 0.0f),
					new Vector3f( 1.0f,  1.0f, 0.0f),
					new Vector3f( 1.0f, -1.0f, 0.0f),
					new Vector3f(-1.0f, -1.0f, 0.0f),
					new Vector3f(-1.0f,  1.0f, 1.0f),
				new Vector3f( 1.0f,  1.0f, 1.0f),
					new Vector3f( 1.0f, -1.0f, 1.0f),
					new Vector3f(-1.0f, -1.0f, 1.0f),
			};

			float prevSplitDist = cascadeIdx == 0 ? MinDistance : CascadeSplits[cascadeIdx - 1];
			float splitDist = CascadeSplits[cascadeIdx];

			Matrix4f invViewProj = CalculateInverseViewProj(camera);

			for(int i = 0; i < 8; ++i)
				frustumCornersWS[i] = Vector3f::Transform(frustumCornersWS[i], invViewProj);

			// Get the corners of the current cascade slice of the view frustum
			for(int i = 0; i < 4; ++i)
			{
				Vector3f cornerRay = frustumCornersWS[i + 4] - frustumCornersWS[i];
				Vector3f nearCornerRay = cornerRay * prevSplitDist;
				Vector3f farCornerRay = cornerRay * splitDist;
				frustumCornersWS[i + 4] = frustumCornersWS[i] + farCornerRay;
				frustumCornersWS[i] = frustumCornersWS[i] + nearCornerRay;
			}

			// Calculate the centroid of the view frustum slice
			Vector3f frustumCenter = 0.0f;
			for(int i = 0; i < 8; ++i)
				frustumCenter = frustumCenter + frustumCornersWS[i];
			frustumCenter *=  1.0f / 8.0f;

			// Pick the up vector to use for the light camera
			Vector3f upDir = camera.Right();

			Vector3f minExtents;
			Vector3f maxExtents;
			if(AppSettings::StabilizeCascades)
			{
				// This needs to be constant for it to be stable
				upDir = Vector3f(0.0f, 1.0f, 0.0f);

				// Calculate the radius of a bounding sphere surrounding the frustum corners
				float sphereRadius = 0.0f;
				for(int i = 0; i < 8; ++i)
				{
					float dist = Vector3f::Length(frustumCornersWS[i] - frustumCenter);
					sphereRadius = std::max(sphereRadius, dist);
				}

				sphereRadius = std::ceil(sphereRadius * 16.0f) / 16.0f;

				maxExtents = Vector3f(sphereRadius, sphereRadius, sphereRadius);
				minExtents = -maxExtents;
			}
			else
			{
				// Create a temporary view matrix for the light
				Vector3f lightCameraPos = frustumCenter;
				Vector3f lookAt = frustumCenter - AppSettings::LightDirection;
				Matrix4f lightView = new Matrix4f().setLookAtLH(lightCameraPos, lookAt, upDir);
					//XMMatrixLookAtLH(lightCameraPos.ToSIMD(), lookAt.ToSIMD(), upDir.ToSIMD());

				// Calculate an AABB around the frustum corners
				Vector3f mins = new Vector3f(Float.MAX_VALUE);
				Vector3f maxes = new Vector3f(-Float.MAX_VALUE);
				for(int i = 0; i < 8; ++i)
				{
					Vector3f corner = lightView.transformPosition(frustumCornersWS[i], new Vector3f());
					mins = mins.min(corner);
					maxes = maxes.max(corner);
				}

				minExtents = mins;
				maxExtents = maxes;

				// Adjust the min/max to accommodate the filtering size
				float scale = (ShadowMapSize + AppSettings::FixedFilterKernelSize()) / static_cast<float>(ShadowMapSize);
				minExtents.x *= scale;
				minExtents.y *= scale;
				maxExtents.x *= scale;
				maxExtents.y *= scale;
			}

			Vector3f cascadeExtents = maxExtents - minExtents;

			// Get position of the shadow camera
			Vector3f shadowCameraPos = frustumCenter + AppSettings::LightDirection.Value() * -minExtents.z;

			// Come up with a new orthographic camera for the shadow caster
			OrthographicCamera shadowCamera(minExtents.x, minExtents.y, maxExtents.x,
			maxExtents.y, 0.0f, cascadeExtents.z);
			shadowCamera.SetLookAt(shadowCameraPos, frustumCenter, upDir);

			if(AppSettings::StabilizeCascades)
			{
				// Create the rounding matrix, by projecting the world-space origin and determining
				// the fractional offset in texel space
				XMMATRIX shadowMatrix = shadowCamera.ViewProjectionMatrix().ToSIMD();
				XMVECTOR shadowOrigin = XMVectorSet(0.0f, 0.0f, 0.0f, 1.0f);
				shadowOrigin = XMVector4Transform(shadowOrigin, shadowMatrix);
				shadowOrigin = XMVectorScale(shadowOrigin, sMapSize / 2.0f);

				XMVECTOR roundedOrigin = XMVectorRound(shadowOrigin);
				XMVECTOR roundOffset = XMVectorSubtract(roundedOrigin, shadowOrigin);
				roundOffset = XMVectorScale(roundOffset, 2.0f / sMapSize);
				roundOffset = XMVectorSetZ(roundOffset, 0.0f);
				roundOffset = XMVectorSetW(roundOffset, 0.0f);

				XMMATRIX shadowProj = shadowCamera.ProjectionMatrix().ToSIMD();
				shadowProj.r[3] = XMVectorAdd(shadowProj.r[3], roundOffset);
				shadowCamera.SetProjection(shadowProj);
			}

			// Draw the mesh with depth only, using the new shadow camera
			RenderDepthCPU(context, shadowCamera, world, characterWorld, true);

			// Apply the scale/offset matrix, which transforms from [-1,1]
			// post-projection space to [0,1] UV space
			Matrix4f texScaleBias = new Matrix4f();
			texScaleBias.setRow(0, new Vector4f(0.5f,  0.0f, 0.0f, 0.0f));
			texScaleBias.setRow(1, new Vector4f(0.0f, -0.5f, 0.0f, 0.0f));
			texScaleBias.setRow(2, new Vector4f(0.0f,  0.0f, 1.0f, 0.0f));
			texScaleBias.setRow(3, new Vector4f(0.5f,  0.5f, 0.0f, 1.0f));
			XMMATRIX shadowMatrix = shadowCamera.ViewProjectionMatrix().ToSIMD();
			shadowMatrix = XMMatrixMultiply(shadowMatrix, texScaleBias);

			// Store the split distance in terms of view space depth
        final float clipDist = camera.FarClip() - camera.NearClip();
			meshPSConstants.Data.CascadeSplits[cascadeIdx] = camera.NearClip() + splitDist * clipDist;

			// Calculate the position of the lower corner of the cascade partition, in the UV space
			// of the first cascade partition
			Matrix4f invCascadeMat = Matrix4f::Invert(shadowMatrix);
			Vector3f cascadeCorner = invCascadeMat.transformPosition(new Vector3f(0.0f, 0.0f, 0.0f));
			cascadeCorner = Vector3f::Transform(cascadeCorner, globalShadowMatrix);

			// Do the same for the upper corner
			Vector3f otherCorner = invCascadeMat.transformPosition(new Vector3f(1.0f, 1.0f, 1.0f));
			otherCorner = globalShadowMatrix.transform(otherCorner);

			// Calculate the scale and offset
			Vector3f cascadeScale = Vector3f(1.0f, 1.0f, 1.0f) / (otherCorner - cascadeCorner);
			meshPSConstants.Data.CascadeOffsets[cascadeIdx] = new Vector4f(-cascadeCorner, 0.0f);
			meshPSConstants.Data.CascadeScales[cascadeIdx] = new Vector4f(cascadeScale, 1.0f);

			if(AppSettings::UseFilterableShadows())
			ConvertToVSM(context, cascadeIdx, meshPSConstants.Data.CascadeScales[cascadeIdx].To3D(),
				meshPSConstants.Data.CascadeScales[0].To3D());
		}
	}*/
}
