import java.io.IOException;
import java.util.ArrayList;

public class Runner {
    public static void main(String[] args) {
        try {
            final ArrayList<Process> processes = new ArrayList<>();
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    processes.forEach(Process::destroy);
                }
            }));
            while(true) {
                ProcessBuilder builder = new ProcessBuilder().inheritIO().command("java", "Runner2");
                Process process = builder.start();
                processes.add(process);
                int exitCode = process.waitFor();
                if (exitCode != -2)
                    break;
            }
        } catch (IOException | InterruptedException e) {

        }
    }
}
