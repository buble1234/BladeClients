package win.blade.common.utils.math;

public record RadiusUtility(float radius1, float radius2, float radius3, float radius4) {

    public static final RadiusUtility NO_ROUND = new RadiusUtility(0.0f, 0.0f, 0.0f, 0.0f);

    public RadiusUtility(double radius1, double radius2, double radius3, double radius4) {
        this((float) radius1, (float) radius2, (float) radius3, (float) radius4);
    }

    public RadiusUtility(double radius) {
        this(radius, radius, radius, radius);
    }

    public RadiusUtility(float radius) {
        this(radius, radius, radius, radius);
    }

}