package ColorioServer;

public class ClientKeys {
    private boolean w;
    private boolean a;
    private boolean s;
    private boolean d;

    public ClientKeys(boolean w, boolean a, boolean s, boolean d) {
        this.w = w;
        this.a = a;
        this.s = s;
        this.d = d;
    }

    public boolean isW() {
        return w;
    }

    public void setW(boolean w) {
        this.w = w;
    }

    public boolean isA() {
        return a;
    }

    public void setA(boolean a) {
        this.a = a;
    }

    public boolean isS() {
        return s;
    }

    public void setS(boolean s) {
        this.s = s;
    }

    public boolean isD() {
        return d;
    }

    public void setD(boolean d) {
        this.d = d;
    }
}
