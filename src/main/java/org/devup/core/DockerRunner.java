package org.devup.core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class DockerRunner {

    private DockerRunner() {}

    public record Result(int exit, String out, String err) {
        public boolean ok() { return exit == 0; }
        public String merged() { return (out + (err.isEmpty() ? "" : "\n" + err)).trim(); }
    }

    public static Result run(String... args) {
        return run(Arrays.asList(args));
    }

    public static Result run(List<String> args) {
        List<String> cmd = new ArrayList<>();
        cmd.add("docker");
        cmd.addAll(args);
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(false);
            Process p = pb.start();
            String out;
            String err;
            try (BufferedReader o = new BufferedReader(new InputStreamReader(p.getInputStream()));
                 BufferedReader e = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                out = o.lines().collect(Collectors.joining("\n"));
                err = e.lines().collect(Collectors.joining("\n"));
            }
            int exit = p.waitFor();
            return new Result(exit, out.trim(), err.trim());
        } catch (Exception ex) {
            return new Result(1, "", "Falha ao executar docker: " + ex.getMessage());
        }
    }

    public static boolean available() {
        return run("version", "--format", "{{.Server.Version}}").ok();
    }
}
