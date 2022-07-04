package com.iluwatar.urm;

import com.google.common.collect.Sets;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DomainClassFinder {

  private static final Logger logger = Logger.getLogger(DomainClassFinder.class.getName());

  private static final String URM_PACKAGE = "com.iluwatar.urm";
  public static boolean ALLOW_FINDING_INTERNAL_CLASSES;

  public static ClassLoader[] classLoaders;

  /**
   * method to find and filter classes using reflections.
   *
   * @param packages    list of packages
   * @param ignores     list of ignores
   * @param classLoader URL classloader object
   * @return list of classes
   */
  public static List<Class<?>> findClasses(final List<String> packages, List<String> ignores,
                                           final URLClassLoader classLoader) {

    var classesList = packages.stream()
            .map(packageName -> getClasses(classLoader, packageName))
            .collect(Collectors.toList());

    return packages.stream()
            .map(packageName -> getClasses(classLoader, packageName))
            .flatMap(Collection::stream)
            .filter(DomainClassFinder::isNotPackageInfo)
            .filter(DomainClassFinder::isNotAnonymousClass)
            .filter((Class<?> clazz) -> {
              try {
                return !ignores.contains(clazz.getName())
                        && !ignores.contains(clazz.getSimpleName());
              } catch (Throwable t) {
                logger.severe("Some crazy class");
              }
              return false;
            })
        .sorted(Comparator.comparing(Class::getName))
        .collect(Collectors.toList());
  }

  private static boolean isNotPackageInfo(Class<?> clazz) {
    return !clazz.getSimpleName().equals("package-info");
  }

  private static boolean isNotAnonymousClass(Class<?> clazz) {
    return !clazz.getSimpleName().equals("");
  }

  private static Set<Class<?>> getClasses(URLClassLoader classLoader, String packageName) {
    FilterBuilder filter = new FilterBuilder().include(FilterBuilder.prefix(packageName));
    if (!isAllowFindingInternalClasses()) {
      filter.exclude(FilterBuilder.prefix(URM_PACKAGE));
    }
    classLoaders = new ClassLoader[]{classLoader};
    ConfigurationBuilder cb = new ConfigurationBuilder()
            .setScanners(new SubTypesScanner(false), new ResourcesScanner())
            .setUrls(ClasspathHelper.forPackage(packageName, classLoaders))
            .filterInputsBy(filter);
    cb.setClassLoaders(classLoaders);
    Reflections reflections = new Reflections(cb);
    return Sets.union(reflections.getSubTypesOf(Object.class),
            reflections.getSubTypesOf(Enum.class));
  }

  public static boolean isAllowFindingInternalClasses() {
    return ALLOW_FINDING_INTERNAL_CLASSES |= Boolean.parseBoolean(
        System.getProperty("DomainClassFinder.allowFindingInternalClasses", "false"));
  }

  private DomainClassFinder() {
    // private constructor for utility class
  }
}
