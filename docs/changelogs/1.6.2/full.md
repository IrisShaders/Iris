# Iris 1.6.2 Changelog (full)

Iris 1.6.2 has released for 1.18.2, 1.19.2, and 1.19.4.

This release was developed by IMS and douira.

## Overview

- Fixed a bug causing maps and text to render black on some devices
- Compress midTexCoord on terrain to improve performance and memory usage
- Fix custom images on shadowcomp
- Fix image sampler state on integer images
- Fallback RenderType impl to avoid runtime error
- Add shader printing on errors to debug mode
- Backport debug mode to all versions supported
- Fix ComputerCraft monitors
- Update to Sodium 0.4.11, improving performance.

### Note: Due to changes in Sodium 0.4.11, all objects will be mipmapped. This is an unavoidable change.
