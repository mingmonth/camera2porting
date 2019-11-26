package yskim.sample.camera2porting;

public class CameraManagerFactory {

    private static AndroidCameraManagerImpl sAndroidCameraManager;

    /**
     * Returns the android camera implementation of {@link CameraManager}.
     *
     * @return The {@link CameraManager} to control the camera device.
     */
    public static synchronized CameraManager getAndroidCameraManager() {
        if (sAndroidCameraManager == null) {
            sAndroidCameraManager = new AndroidCameraManagerImpl();
        }
        return sAndroidCameraManager;
    }
}

