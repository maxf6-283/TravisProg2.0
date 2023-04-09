package Display;

import javax.swing.JButton;

public abstract class SheetHandlerJButton<T> extends JButton {
    private T genericThing;

    public SheetHandlerJButton(String text, T genericThing) {
        super(text);
        setgenericThing(genericThing);
    }

    public void setgenericThing(T genericThing) {
        this.genericThing = genericThing;
    }

    public T getgenericThing() {
        return genericThing;
    }
}
