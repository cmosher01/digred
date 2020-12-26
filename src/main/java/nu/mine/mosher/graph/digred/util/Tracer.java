package nu.mine.mosher.graph.digred.util;

public class Tracer {
    private static final boolean TRACE = true;
    public static void trace(final String s) {
        if (TRACE) {
            System.out.println(s);
            System.out.flush();
        }
    }
}
