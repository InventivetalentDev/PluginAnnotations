package org.inventivetalent.pluginannotations.description.processor;

import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.pluginannotations.description.*;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SupportedAnnotationTypes({ "org.inventivetalent.pluginannotations.description.*" })
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class DescriptionProcessor extends AbstractProcessor {

	final SimpleDateFormat    simpleDateFormat = new SimpleDateFormat("dd.MM.yyy HH:mm:ss");
	final Map<String, Object> yamlMap          = new HashMap<>();

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		long generateStart = System.currentTimeMillis();

		Element mainElement = null;
		for (Element element : roundEnv.getElementsAnnotatedWith(Plugin.class)) {
			if (mainElement != null) {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Multiple @Plugin classes found!");
				return false;
			}
			mainElement = element;
		}
		if (mainElement == null) { return false; }
		if (!(mainElement instanceof TypeElement)) {
			processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@Plugin is not a TypeElement");
			return false;
		}
		TypeElement mainTypeElement = (TypeElement) mainElement;
		if (!processingEnv.getTypeUtils().isSubtype(mainTypeElement.asType(), processingEnv.getElementUtils().getTypeElement(JavaPlugin.class.getName()).asType())) {
			processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@Plugin does not extend JavaPlugin");
			return false;
		}

		processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generating plugin.yml for " + mainTypeElement.getQualifiedName().toString() + "...");

		{//// @Plugin - Start
			// Main
			String mainClass = getAnnotationValue(mainTypeElement, "main", Plugin.class, String.class);
			if (mainClass == null || mainClass.isEmpty()) {
				mainClass = mainTypeElement.getQualifiedName().toString();
			}
			yamlMap.put("main", mainClass);

			// Name
			appendAnnotation("name", mainTypeElement, "name", Plugin.class, String.class);

			// Version
			appendAnnotation("version", mainTypeElement, "version", "0.0.0", Plugin.class, String.class);
		}//// @Plugin - End

		{//// @Description - Start
			appendAnnotation("description", mainTypeElement, "value", Description.class, String.class);
		}//// @Description - End

		{//// @Load - Start
			appendAnnotation("load", mainTypeElement, "value", Description.class, String.class);
		}//// @Load - End

		{//// @Author - Start
			//			appendAnnotation("authors", mainTypeElement, "value", new String[0], Author.class, String[].class);
			String[] authors = getAnnotationValue(mainTypeElement, "value", Author.class, String[].class);
			if (authors != null && authors.length > 0) {
				if (authors.length == 1) {
					yamlMap.put("author", authors[0]);
				} else {
					yamlMap.put("authors", authors);
				}
			}
		}//// @Author - End

		{//// @Website - Start
			appendAnnotation("website", mainTypeElement, "value", Website.class, String.class);
		}//// @Website - End

		{//// @Database - Start
			appendAnnotation("database", mainTypeElement, "value", Database.class, boolean.class);
		}//// @Database - End

		{//// @Depend - Start
			//			appendAnnotation("depend", mainTypeElement, "value", new String[0], Depend.class, String[].class);
			String[] depend = getAnnotationValue(mainTypeElement, "value", Depend.class, String[].class);
			if (depend != null && depend.length > 0) {
				yamlMap.put("depend", depend);
			}
		}//// @Depend - End

		{//// @SoftDepend - Start
			//			appendAnnotation("softdepend", mainTypeElement, "value", new String[0], SoftDepend.class, String[].class);
			String[] softdepend = getAnnotationValue(mainTypeElement, "value", SoftDepend.class, String[].class);
			if (softdepend != null && softdepend.length > 0) {
				yamlMap.put("softdepend", softdepend);
			}
		}//// @SoftDepend - End

		{//// @LoadBefore - Start
			//			appendAnnotation("loadbefore", mainTypeElement, "value", new String[0], LoadBefore.class, String[].class);
			String[] loadbefore = getAnnotationValue(mainTypeElement, "value", LoadBefore.class, String[].class);
			if (loadbefore != null && loadbefore.length > 0) {
				yamlMap.put("loadbefore", loadbefore);
			}
		}//// @LoadBefore - End

		{//// @Prefix - Start
			appendAnnotation("prefix", mainTypeElement, "value", Prefix.class, String.class);
		}//// @Prefix - End

		try {
			Yaml yaml = new Yaml();
			try {
				Yaml templateYaml = new Yaml();
				FileObject sourceDescriptionFile = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", "plugin.yml");
				if (sourceDescriptionFile != null) {
					InputStream inputStream = sourceDescriptionFile.openInputStream();
					yamlMap.putAll((Map<String, Object>) templateYaml.load(inputStream));
					inputStream.close();
				}
			} catch (FileNotFoundException e) {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "No plugin.yml template found");
			}
			FileObject descriptionFile = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "plugin.yml");
			try (Writer writer = descriptionFile.openWriter()) {
				writer.append("# plugin.yml generated on ").append(simpleDateFormat.format(new Date())).append(" by PluginAnnotations (https://r.spiget.org/20446) \r\n");
				yaml.dump(yamlMap, writer);
				writer.append("## Generated in ").append(String.valueOf(System.currentTimeMillis() - generateStart)).append("ms");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return true;
	}

	<A extends Annotation, V> V appendAnnotation(String key, Element element, String valueName, Class<A> annotationClass, Class<V> valueClass) {
		return appendAnnotation(key, element, valueName, null, annotationClass, valueClass);
	}

	<A extends Annotation, V> V appendAnnotation(String key, Element element, String valueName, V defaultValue, Class<A> annotationClass, Class<V> valueClass) {
		V value = getAnnotationValue(element, valueName, annotationClass, valueClass);
		if (value != null) {
			yamlMap.put(key, value);
		} else if (defaultValue != null) {
			yamlMap.put(key, defaultValue);
		}
		return value;
	}

	<A extends Annotation, V> V getAnnotationValue(Element element, String valueName, Class<A> annotationClass, Class<V> valueClass) {
		Annotation annotation = element.getAnnotation(annotationClass);
		if (annotation != null) {
			try {
				Object value = annotationClass.getDeclaredMethod(valueName).invoke(annotation);
				//noinspection unchecked
				return (V) (valueClass == String.class ? value.toString() : valueClass.cast(value));
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

}
