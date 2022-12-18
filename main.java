import java.util.concurrent.TimeUnit;

// public class main {
//     public static void main(String[] args) {
//         test tst = new test("D:/EASY_SENSE/EasySenseGUI/V2/data/sound/995.wav");
//         Thread t = new Thread(tst, "test");
//         t.start();
//         System.out.println("done");
//         // try{
//         //     //t.playSound();
//         //     t.start();
//         //     System.out.println("done");
//         // }
//         // catch(Exception e)
//         // {

//         // }
//     }
// }

public class main{
    public static void main(String[] args) throws InterruptedException
    {
        MediaPlayer msound = new MediaPlayer("D:/EASY_SENSE/EasySenseGUI/V2/data/sound/995.wav");
        msound.start();
        System.out.println("done");
        TimeUnit.SECONDS.sleep(10);
        msound.stopLoop();
    }
}