import java.io.IOException;
import java.util.ArrayList;

/**
 * allows the main application to restart by giving an exit code of -2
 */
public class Runner {
    public static void main(String[] args) {
        try {
            // arrayList of all provesses started
            final ArrayList<Process> processes = new ArrayList<>();
            // when this Java program is shutdown, destroy all the processes of the actual
            // application
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    processes.forEach(Process::destroy);
                }
            }));
            while (true) {
                // runs the actual application
                ProcessBuilder builder = new ProcessBuilder().inheritIO().command("java", "Runner2");
                Process process = builder.start();
                processes.add(process);
                int exitCode = process.waitFor();
                // restarts the application of the exit code is -2(restart code)
                if (exitCode != -2)
                    break;
            }
        } catch (IOException | InterruptedException e) {

        }
    }
}
