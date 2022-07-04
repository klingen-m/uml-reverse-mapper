package com.iluwatar.urm;

import com.iluwatar.urm.helper.ClassPathHacker;
import com.iluwatar.urm.helper.FileUtility;
import com.iluwatar.urm.helper.ZipUtility;
import com.iluwatar.urm.presenters.Presenter;
import com.iluwatar.urm.presenters.Representation;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class DomainMapperCli {

  private File deleteMeLater = null;


  private static final Logger log = LoggerFactory.getLogger(DomainMapperCli.class);

  public static void main(final String[] args) throws ClassNotFoundException, IOException {
    new DomainMapperCli().run(args);
  }

  /**
   * run method for cli class.
   * @param args input arguments
   * @throws ClassNotFoundException exception
   * @throws IOException exception
   */
  public void run(final String[] args) throws ClassNotFoundException, IOException {
    Options options = new Options();
    try {
      CommandLineParser parser = new DefaultParser();
      options.addOption("f", "file", true, "write to file");
      options.addOption("p", "package", true, "comma separated list of domain packages");
      options.addOption("i", "ignore", true, "comma separated list of ignored types");
      options.addOption("s", "presenter", true, "presenter to be used");
      options.addOption("w", "war-file(s)", true, "war archive(s) to analyse");

      CommandLine line = parser.parse(options, args);
      String[] packages = getPackagesFromCli(line);
      String[] ignores = ignoresFromCli(line);
      URLClassLoader classLoader = handleWarFiles(line);

      Presenter presenter = Presenter.parse(line.getOptionValue("s"));
      DomainMapper domainMapper = DomainMapper.create(presenter,
              Arrays.asList(packages),
              ignores == null ? new ArrayList<>() : Arrays.asList(ignores),
              classLoader);
      Representation representation = domainMapper.describeDomain();
      writeOutput(line, representation);
    } catch (ParseException exp) {
      log.info(exp.getMessage());
      // automatically generate the help statement
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("java -jar urm-core.jar", options);
    }
    if (deleteMeLater != null) {
      deleteMeLater.delete();
    }
  }

  private void writeOutput(CommandLine line, Representation representation) throws IOException {
    if (line.hasOption('f')) {
      String filename = line.getOptionValue('f');
      Path p = Path.of(filename);
      Path parent = p.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      Files.write(p, representation.getContent().getBytes());
      log.info("Wrote to file " + filename);
    } else {
      log.info(representation.getContent());
    }
  }

  private URLClassLoader handleWarFiles(CommandLine line) {
    URLClassLoader classLoader = null;
    //deleteMeLater = null;
    if (line.hasOption("w")) {
      String[] warFiles = line.getOptionValue("w").split(",[ ]*");
      classLoader = ClassPathHacker.getInstance().getClassLoader();
      log.debug("Scanning war files:");
      for (String warFile : warFiles) {
        log.debug(warFile);
        try {
          File f = new File(warFile);
          String folderName = f.getName().replace(".war", "");
          deleteMeLater = new File(f.getParentFile(), folderName);
          deleteMeLater.mkdirs();
          deleteMeLater.deleteOnExit();//doesn't work reliable
          ZipUtility.unzip(warFile, deleteMeLater);
          ClassPathHacker.getInstance().addFile(new File(new File(deleteMeLater, "WEB-INF"), "classes"));
          for (File classFile : FileUtility.listDirectory(deleteMeLater, Set.of(".jar"))) { //".class"
            ClassPathHacker.getInstance().addFile(classFile);
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return classLoader;
  }

  private String[] ignoresFromCli(CommandLine line) {
    String[] ignores = null;
    if (line.hasOption("i")) {
      ignores = line.getOptionValue("i").split(",[ ]*");
      log.debug("Ignored types:");
      for (String ignore : ignores) {
        log.debug(ignore);
      }
    }
    return ignores;
  }

  private String[] getPackagesFromCli(CommandLine line) {
    String[] packages = line.getOptionValue("p").split(",[ ]*");
    log.debug("Scanning domain for packages:");
    for (String packageName : packages) {
      log.debug(packageName);
    }
    return packages;
  }
}
