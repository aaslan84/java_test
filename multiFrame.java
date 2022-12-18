import java.awt.Color;

public class multiFrame {
    public static void main(String[] args)
    {
        TankInfo sFrame = new TankInfo(0);
        sFrame.frame.setResizable(false);
        
        sFrame.configAddStr("Hello there", true);
        sFrame.configAddStr("Hello there", true);
        sFrame.configAddStr("Hello there", true);
        // sFrame.configClrStr();

        sFrame.calibAddStr("Hello there", true);
        sFrame.calibAddStr("Hello there", true);
        sFrame.calibAddStr("Hello there", true);
        // sFrame.calibClrStr();

        sFrame.eventAddStr("Hello there", Color.BLUE, true);
        sFrame.eventAddStr("Hello there", Color.RED, true);
        sFrame.eventAddStr("Hello there", Color.CYAN, true);
        // sFrame.eventClrStr();
    }
}
