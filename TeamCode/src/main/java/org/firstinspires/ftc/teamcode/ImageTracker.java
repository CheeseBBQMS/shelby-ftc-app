package org.firstinspires.ftc.teamcode;

import com.qualcomm.ftccommon.DbgLog;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ImageTracker
{
    ImageTracker()
    {
        setupTrackables();
        setupPhoneOnRobot();
    }

    public void setupTrackables()
    {
        //To see camera feedback, pass the view id
        //For competition, we don't want this - so use the no param ctor
        parameters = new VuforiaLocalizer.Parameters(com.qualcomm.ftcrobotcontroller.R.id.cameraMonitorViewId);
        //new VuforiaLocalizer.Parameters();

        //SJH Teams license
        parameters.vuforiaLicenseKey =
                "AQgIvJ7/////AAAAGQSociXWO0kDvfP15zd4zOsS+fHJygDMLA" +
                        "1HhOJQ3FkeiPLGU6YW3ru+jzC6MGxM5tY1ajF4Y0plOpxhQGfS" +
                        "R4g3zFiP0IQavezWhGbjBCRMmYu8INy8KvoZ03crZe9wxxQJu9" +
                        "9KiNX3ZrbUevNXODKKzWyA9RqxxQHbJ3gpXoff4z1O9n211VOg" +
                        "EsJjrNZq8xJnznilyXwc8colJnZD/Adr6UmOzxoUGgaMrdPrlj" +
                        "McDJZU6uyoIrOjiv1G2r3iNjtd7LzKAANKrK/0IrO90MgRqQDr" +
                        "CAAJVHqqyyubMy8EqE5onzw/WFEcEwfQ6nolsNwYTEZb/JppU8" +
                        "9Q6DZmhz4FCT49shA+4PyNOzqsjhRC";

        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;
        this.vuforia = ClassFactory.createVuforiaLocalizer(parameters);
        DbgLog.msg("SJH Vuforia LicKey: " + parameters.vuforiaLicenseKey);

        ftcImages = this.vuforia.loadTrackablesFromAsset("FTC_2016-17");
        //Wheels are on blue side closest to blue corner
        blueWheels = ftcImages.get(0);
        blueWheels.setName("BlueWheels");

        //Legos are on blud side furthest from blue corner
        blueLegos = ftcImages.get(2);
        blueLegos.setName("BlueLegos");

        //Tools are on red side furthest from red corner
        redTools = ftcImages.get(1);
        redTools.setName("RedTools");

        //Gears are on red side closest to red corner
        redGears = ftcImages.get(3);
        redGears.setName("RedGears");

        allTrackables.addAll(ftcImages);

        redTools.setLocation(Field.redToolsLocationOnField);
        RobotLog.ii(TAG, "Red Tools=%s", format(Field.redToolsLocationOnField));

        redGears.setLocation(Field.redToolsLocationOnField);
        RobotLog.ii(TAG, "Red Gears=%s", format(Field.redGearsLocationOnField));

        blueWheels.setLocation(Field.blueWheelsLocationOnField);
        RobotLog.ii(TAG, "Blue Wheels=%s", format(Field.blueWheelsLocationOnField));

        blueLegos.setLocation(Field.blueLegosLocationOnField);
        RobotLog.ii(TAG, "Blue Legos=%s", format(Field.blueLegosLocationOnField));
    }

    public void setupPhoneOnRobot()
    {
        OpenGLMatrix phoneLocationOnRobot = ShelbyBot.phoneLocationOnRobot;
        RobotLog.ii(TAG, "phone=%s", format(phoneLocationOnRobot));

        /**
         * A brief tutorial: here's how all the math is going to work:
         *
         * C = phoneLocationOnRobot     maps   phone coords        -> robot coords
         * P = tracker.getPose()        maps   image target coords -> phone coords
         * L = redTargetLocationOnField maps   image target coords -> field coords
         *
         * So
         *
         * C.inverted()                 maps   robot coords -> phone coords
         * P.inverted()                 maps   phone coords -> imageTarget coords
         *
         * Putting that all together,
         *
         * L x P.inverted() x C.inverted() maps robot coords to field coords.
         *
         * @see VuforiaTrackableDefaultListener#getRobotLocation()
         */

        ((VuforiaTrackableDefaultListener)redTools.getListener()).
                setPhoneInformation(phoneLocationOnRobot, parameters.cameraDirection);
        ((VuforiaTrackableDefaultListener)redGears.getListener()).
                setPhoneInformation(phoneLocationOnRobot, parameters.cameraDirection);
        ((VuforiaTrackableDefaultListener)blueLegos.getListener()).
                setPhoneInformation(phoneLocationOnRobot, parameters.cameraDirection);
        ((VuforiaTrackableDefaultListener)blueWheels.getListener()).
                setPhoneInformation(phoneLocationOnRobot, parameters.cameraDirection);
    }

    public OpenGLMatrix getRobotLocation()
    {
        /**
         * getUpdatedRobotLocation() will return null if no new information is available
         * since the last time that call was made, or if the trackable is not currently
         * visible.
         * getRobotLocation() will return null if the trackable is not currently visible.
         */
        OpenGLMatrix robotLocationTransform = null;
        for (VuforiaTrackable trackable : allTrackables)
        {
            robotLocationTransform = ((VuforiaTrackableDefaultListener) trackable.getListener())
                    .getUpdatedRobotLocation();
            if(robotLocationTransform != null)
            {
                lastVisName = trackable.getName();
                break;
            }
        }
        return robotLocationTransform;
    }

    public String getLocStirng(OpenGLMatrix mat)
    {
        String locStr = null;
        if(mat != null)
        {
            float xyz[] = mat.getTranslation().getData();
            Orientation ori = Orientation.getOrientation(lastLocation,
                    AxesReference.EXTRINSIC, AxesOrder.ZXY, AngleUnit.DEGREES);

            locStr = String.format(Locale.US,
                    "%10s POS: %5.2f, %5.2f, %5.2f ROT: %4.1f, %4.1f, %4.1f",
                    lastVisName, xyz[0] / MM_PER_INCH, xyz[1] / MM_PER_INCH, xyz[2] / MM_PER_INCH,
                    ori.firstAngle, ori.secondAngle, ori.thirdAngle);
        }
        return locStr;
    }

    public void activate()
    {
        ftcImages.activate();
    }

    public void deactivate()
    {
        ftcImages.deactivate();
    }

    String format(OpenGLMatrix transformationMatrix)
    {
        return transformationMatrix.formatAsTransform();
    }

    // Vuforia units are mm = units used in XML for the trackables
    private static final float MM_PER_INCH        = 25.4f;
    public static final String TAG = "SJH Image Tracker";

    private VectorF currPos = new VectorF(0.0f, 0.0f, 0.0f);
    private List<VuforiaTrackable> allTrackables = new ArrayList<>();
    private OpenGLMatrix lastLocation = null;
    private VuforiaLocalizer vuforia;
    private VuforiaTrackable blueWheels;
    private VuforiaTrackable blueLegos;
    private VuforiaTrackable redTools;
    private VuforiaTrackable redGears;
    private VuforiaLocalizer.Parameters parameters;
    private VuforiaTrackables ftcImages;
    private String lastVisName = "UNKNOWN";
}
