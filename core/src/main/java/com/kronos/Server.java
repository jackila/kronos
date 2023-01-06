package com.kronos;

import com.kronos.jobgraph.JobConfiguration;
import com.kronos.jobgraph.logical.LogicalGraph;
import com.kronos.runtime.jobmaster.JobMaster;
import java.io.File;
import lombok.SneakyThrows;
import picocli.CommandLine;

/**
 * 可以设计成一个分布式任务处理集群，由客户端提交任务，本地执行
 *
 * <p>like canal manager --- canal instance
 */
public class Server implements Runnable {

    @CommandLine.Parameters(
            arity = "1",
            paramLabel = "configFile",
            description = "config File(such as " + "student_example.yml) to process.")
    private File configFile;

    // job master
    public static void main(String[] args) {

        // By implementing Runnable or Callable, parsing, error handling and handling user
        // requests for usage help or version help can be done with one line of code.

        int exitCode = new CommandLine(new Server()).execute(args);
        System.exit(exitCode);
    }

    @Override
    @SneakyThrows
    public void run() {

        JobConfiguration config = JobConfiguration.load(configFile.getAbsolutePath());
        LogicalGraph graph = LogicalGraph.instance(config);
        JobMaster jobMaster = new JobMaster(graph.getSources());
        jobMaster.execute(graph);
    }
}
