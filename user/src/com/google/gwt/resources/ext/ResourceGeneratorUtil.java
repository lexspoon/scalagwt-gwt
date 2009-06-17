/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.resources.ext;

import com.google.gwt.core.ext.BadPropertyValueException;
import com.google.gwt.core.ext.PropertyOracle;
import com.google.gwt.core.ext.SelectionProperty;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JPackage;
import com.google.gwt.dev.resource.Resource;
import com.google.gwt.dev.resource.ResourceOracle;
import com.google.gwt.resources.client.ClientBundle.Source;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utility methods for building ResourceGenerators.
 */
public final class ResourceGeneratorUtil {

  private static class ClassLoaderLocator implements Locator {
    private final ClassLoader classLoader;

    public ClassLoaderLocator(ClassLoader classLoader) {
      this.classLoader = classLoader;
    }

    public URL locate(String resourceName) {
      return classLoader.getResource(resourceName);
    }
  }

  /**
   * Wrapper interface around different strategies for loading resource data.
   */
  private interface Locator {
    URL locate(String resourceName);
  }

  private static class ResourceOracleLocator implements Locator {
    private final Map<String, Resource> resourceMap;

    public ResourceOracleLocator(ResourceOracle oracle) {
      resourceMap = oracle.getResourceMap();
    }

    public URL locate(String resourceName) {
      Resource r = resourceMap.get(resourceName);
      return r == null ? null : r.getURL();
    }
  }

  /**
   * These are type names from previous APIs or from APIs with similar
   * functionality that might be confusing.
   * 
   * @see #checkForDeprecatedAnnotations
   */
  private static final String[] DEPRECATED_ANNOTATION_NAMES = {
      "com.google.gwt.libideas.resources.client.ImmutableResourceBundle$Resource",
      "com.google.gwt.user.client.ui.ImageBundle$Resource"};

  private static final List<Class<? extends Annotation>> DEPRECATED_ANNOTATION_CLASSES;

  static {
    List<Class<? extends Annotation>> classes = new ArrayList<Class<? extends Annotation>>(
        DEPRECATED_ANNOTATION_NAMES.length);

    for (String name : DEPRECATED_ANNOTATION_NAMES) {
      try {
        Class<?> maybeAnnotation = Class.forName(name, false,
            ResourceGeneratorUtil.class.getClassLoader());

        // Possibly throws ClassCastException
        Class<? extends Annotation> annotationClass = maybeAnnotation.asSubclass(Annotation.class);

        classes.add(annotationClass);

      } catch (ClassCastException e) {
        // If it's not an Annotation type, we don't care about it
      } catch (ClassNotFoundException e) {
        // This is OK; the annotation doesn't exist.
      }
    }

    if (classes.isEmpty()) {
      DEPRECATED_ANNOTATION_CLASSES = Collections.emptyList();
    } else {
      DEPRECATED_ANNOTATION_CLASSES = Collections.unmodifiableList(classes);
    }
  }

  /**
   * Return the base filename of a resource. The behavior is similar to the unix
   * command <code>basename</code>.
   * 
   * @param resource the URL of the resource
   * @return the final name segment of the resource
   */
  public static String baseName(URL resource) {
    String path = resource.getPath();
    return path.substring(path.lastIndexOf('/') + 1);
  }

  /**
   * Find all resources referenced by a method in a bundle. The method's
   * {@link Source} annotation will be examined and the specified locations will
   * be expanded into URLs by which they may be accessed on the local system.
   * <p>
   * This method is sensitive to the <code>locale</code> deferred-binding
   * property and will attempt to use a best-match lookup by removing locale
   * components.
   * <p>
   * Loading through a ClassLoader with this method is much slower than the
   * other <code>findResources</code> methods which make use of the compiler's
   * ResourceOracle.
   * 
   * @param logger a TreeLogger that will be used to report errors or warnings
   * @param context the ResourceContext in which the ResourceGenerator is
   *          operating
   * @param classLoader the ClassLoader to use when locating resources
   * @param method the method to examine for {@link Source} annotations
   * @param defaultSuffixes if the supplied method does not have any
   *          {@link Source} annotations, act as though a Source annotation was
   *          specified, using the name of the method and each of supplied
   *          extensions in the order in which they are specified
   * @return URLs for each {@link Source} annotation value defined on the
   *         method.
   * @throws UnableToCompleteException if ore or more of the sources could not
   *           be found. The error will be reported via the <code>logger</code>
   *           provided to this method
   */
  public static URL[] findResources(TreeLogger logger, ClassLoader classLoader,
      ResourceContext context, JMethod method, String[] defaultSuffixes)
      throws UnableToCompleteException {
    return findResources(logger, new ClassLoaderLocator(classLoader), context,
        method, defaultSuffixes, true);
  }

  /**
   * Find all resources referenced by a method in a bundle. The method's
   * {@link Source} annotation will be examined and the specified locations will
   * be expanded into URLs by which they may be accessed on the local system.
   * <p>
   * This method is sensitive to the <code>locale</code> deferred-binding
   * property and will attempt to use a best-match lookup by removing locale
   * components.
   * <p>
   * The compiler's ResourceOracle will be used to resolve resource locations.
   * If the desired resource cannot be found in the ResourceOracle, this method
   * will fall back to using the current thread's context ClassLoader. If it is
   * necessary to alter the way in which resources are located, use the overload
   * that accepts a ClassLoader.
   * 
   * @param logger a TreeLogger that will be used to report errors or warnings
   * @param context the ResourceContext in which the ResourceGenerator is
   *          operating
   * @param method the method to examine for {@link Source} annotations
   * @return URLs for each {@link Source} annotation value defined on the
   *         method.
   * @throws UnableToCompleteException if ore or more of the sources could not
   *           be found. The error will be reported via the <code>logger</code>
   *           provided to this method
   */
  public static URL[] findResources(TreeLogger logger, ResourceContext context,
      JMethod method) throws UnableToCompleteException {
    return findResources(logger, context, method, new String[0]);
  }

  /**
   * Find all resources referenced by a method in a bundle. The method's
   * {@link Source} annotation will be examined and the specified locations will
   * be expanded into URLs by which they may be accessed on the local system.
   * <p>
   * This method is sensitive to the <code>locale</code> deferred-binding
   * property and will attempt to use a best-match lookup by removing locale
   * components.
   * <p>
   * The compiler's ResourceOracle will be used to resolve resource locations.
   * If the desired resource cannot be found in the ResourceOracle, this method
   * will fall back to using the current thread's context ClassLoader. If it is
   * necessary to alter the way in which resources are located, use the overload
   * that accepts a ClassLoader.
   * 
   * @param logger a TreeLogger that will be used to report errors or warnings
   * @param context the ResourceContext in which the ResourceGenerator is
   *          operating
   * @param method the method to examine for {@link Source} annotations
   * @param defaultSuffixes if the supplied method does not have any
   *          {@link Source} annotations, act as though a Source annotation was
   *          specified, using the name of the method and each of supplied
   *          extensions in the order in which they are specified
   * @return URLs for each {@link Source} annotation value defined on the
   *         method.
   * @throws UnableToCompleteException if ore or more of the sources could not
   *           be found. The error will be reported via the <code>logger</code>
   *           provided to this method
   */
  public static URL[] findResources(TreeLogger logger, ResourceContext context,
      JMethod method, String[] defaultSuffixes)
      throws UnableToCompleteException {

    // Try to find the resources with ResourceOracle
    Locator locator = new ResourceOracleLocator(
        context.getGeneratorContext().getResourcesOracle());

    // Don't report errors since we have a fallback mechanism
    URL[] toReturn = findResources(logger, locator, context, method,
        defaultSuffixes, false);

    if (toReturn == null) {
      // Since not all resources were found, try with ClassLoader
      locator = new ClassLoaderLocator(
          Thread.currentThread().getContextClassLoader());

      // Do report hard failures
      toReturn = findResources(logger, locator, context, method,
          defaultSuffixes, true);
    }
    return toReturn;
  }

  /**
   * We want to warn the user about any annotations from ImageBundle or the old
   * incubator code.
   */
  private static void checkForDeprecatedAnnotations(TreeLogger logger,
      JMethod method) {

    for (Class<? extends Annotation> annotationClass : DEPRECATED_ANNOTATION_CLASSES) {
      if (method.isAnnotationPresent(annotationClass)) {
        logger.log(TreeLogger.WARN, "Deprecated annotation used; expecting "
            + Source.class.getCanonicalName() + " but found "
            + annotationClass.getName() + " instead.  It is likely "
            + "that undesired operation will occur.");
      }
    }
  }

  /**
   * Main implementation of findResources.
   * 
   * @param reportErrors controls whether or not the inability to locate any
   *          given resource should be a hard error (throw an UnableToComplete)
   *          or a soft error (return null).
   */
  private static URL[] findResources(TreeLogger logger, Locator locator,
      ResourceContext context, JMethod method, String[] defaultSuffixes,
      boolean reportErrors) throws UnableToCompleteException {
    logger = logger.branch(TreeLogger.DEBUG, "Finding resources");

    String locale;
    try {
      PropertyOracle oracle = context.getGeneratorContext().getPropertyOracle();
      SelectionProperty prop = oracle.getSelectionProperty(logger, "locale");
      locale = prop.getCurrentValue();
    } catch (BadPropertyValueException e) {
      locale = null;
    }

    checkForDeprecatedAnnotations(logger, method);

    boolean error = false;
    Source resourceAnnotation = method.getAnnotation(Source.class);
    URL[] toReturn;

    if (resourceAnnotation == null) {
      if (defaultSuffixes != null) {
        for (String extension : defaultSuffixes) {
          logger.log(TreeLogger.SPAM, "Trying default extension " + extension);
          URL resourceUrl = tryFindResource(locator, getPathRelativeToPackage(
              method.getEnclosingType().getPackage(), method.getName()
                  + extension), locale);

          if (resourceUrl != null) {
            // Early out because we found a hit
            return new URL[] {resourceUrl};
          }
        }
      }

      if (reportErrors) {
        logger.log(TreeLogger.ERROR, "No " + Source.class.getName()
            + " annotation and no resources found with default extensions");
      }
      toReturn = null;
      error = true;

    } else {
      // The user has put an @Source annotation on the accessor method
      String[] resources = resourceAnnotation.value();

      toReturn = new URL[resources.length];

      int tagIndex = 0;
      for (String resource : resources) {
        // Try to find the resource relative to the package.
        URL resourceURL = tryFindResource(locator, getPathRelativeToPackage(
            method.getEnclosingType().getPackage(), resource), locale);

        // If we didn't find the resource relative to the package, assume it is
        // absolute.
        if (resourceURL == null) {
          resourceURL = tryFindResource(locator, resource, locale);
        }

        if (resourceURL == null) {
          error = true;
          if (reportErrors) {
            logger.log(TreeLogger.ERROR, "Resource " + resource
                + " not found. Is the name specified as Class.getResource()"
                + " would expect?");
          } else {
            // Speculative attempts should not emit errors
            logger.log(TreeLogger.DEBUG, "Stopping because " + resource
                + " not found");
          }
        }

        toReturn[tagIndex++] = resourceURL;
      }
    }

    if (error && reportErrors) {
      throw new UnableToCompleteException();
    }

    return toReturn;
  }

  /**
   * Converts a package relative path into an absolute path.
   * 
   * @param pkg the package
   * @param path a path relative to the package
   * @return an absolute path
   */
  private static String getPathRelativeToPackage(JPackage pkg, String path) {
    return pkg.getName().replace('.', '/') + '/' + path;
  }

  /**
   * This performs the locale lookup function for a given resource name.
   * 
   * @param locator the Locator to use to load the resources
   * @param resourceName the string name of the desired resource
   * @param locale the locale of the current rebind permutation
   * @return a URL by which the resource can be loaded, <code>null</code> if one
   *         cannot be found
   */
  private static URL tryFindResource(Locator locator, String resourceName,
      String locale) {
    URL toReturn = null;

    // Look for locale-specific variants of individual resources
    if (locale != null) {
      // Convert language_country_variant to independent pieces
      String[] localeSegments = locale.split("_");
      int lastDot = resourceName.lastIndexOf(".");
      String prefix = lastDot == -1 ? resourceName : resourceName.substring(0,
          lastDot);
      String extension = lastDot == -1 ? "" : resourceName.substring(lastDot);

      for (int i = localeSegments.length - 1; i >= -1; i--) {
        String localeInsert = "";
        for (int j = 0; j <= i; j++) {
          localeInsert += "_" + localeSegments[j];
        }

        toReturn = locator.locate(prefix + localeInsert + extension);
        if (toReturn != null) {
          break;
        }
      }
    } else {
      toReturn = locator.locate(resourceName);
    }

    return toReturn;
  }

  /**
   * Utility class.
   */
  private ResourceGeneratorUtil() {
  }
}
