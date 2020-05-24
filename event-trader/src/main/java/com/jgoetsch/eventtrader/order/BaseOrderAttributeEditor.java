package com.jgoetsch.eventtrader.order;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class BaseOrderAttributeEditor<C> extends PropertyEditorSupport {

	private final String suffix;
	private final Class<C> targetClass;
	private final Pattern pattern;

	public BaseOrderAttributeEditor(Class<C> targetClass, String suffix, Pattern pattern) {
		this.targetClass = targetClass;
		this.suffix = suffix;
		this.pattern = pattern;
	}

	private String getFullClassName(String className) {
		StringBuilder fullName = new StringBuilder();
		if (!className.contains("."))
			fullName.append(targetClass.getPackage().getName()).append(".");
		fullName.append(className);
		if (suffix != null && !className.endsWith(suffix))
			fullName.append(suffix);
		return fullName.toString();
	}

	protected abstract C createFixed(String specifier);
	protected abstract void applyModifier(C target, String operation, BigDecimal value, String originalModifier);
	protected abstract C collectResults(Collection<C> results);

	static final Pattern numeric = Pattern.compile("\\$?[\\d\\.]+");

	private C parseSpecifier(String specifier) {
		Matcher m = pattern.matcher(specifier);
		if (m.matches()) {
			String typeDef = m.group(1);
			String operation = m.group(2);
			String modifier = m.group(3);

			C target;
			if (numeric.matcher(typeDef).matches()) {
				target = createFixed(typeDef);
			}
			else {
				try {
					Class<? extends C> clz = Class.forName(getFullClassName(typeDef)).asSubclass(targetClass);
					target = clz.getDeclaredConstructor().newInstance();
				} catch (ClassCastException e) {
					throw new IllegalArgumentException(typeDef + " is not an implementation of " + targetClass.getSimpleName(), e);
				} catch (ReflectiveOperationException e) {
					throw new IllegalArgumentException(targetClass.getSimpleName() + " class \"" + typeDef + "\" cannot be instantiated", e);
				}

				if (modifier != null && modifier.length() > 0) {
					NumberFormat format = modifier.endsWith("%") ? NumberFormat.getPercentInstance() : NumberFormat.getNumberInstance();
					((DecimalFormat)format).setParseBigDecimal(true);
					try {
						applyModifier(target, operation, (BigDecimal)format.parse(modifier), modifier);
					} catch (ParseException e) {
						throw new IllegalArgumentException("Invalid modifier on " + targetClass.getSimpleName() + " (must be decimal number or percentage): " + e.getMessage());
					}
				}
			}
			return target;
		}
		else
			throw new IllegalArgumentException("Invalid " + targetClass.getSimpleName() + " specifier: " + specifier);
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		C value = Arrays.stream(text.split("\\s*,\\s*")).map(this::parseSpecifier)
			.collect(Collectors.collectingAndThen(Collectors.toList(), this::collectResults));
		
		if (value != null)
			setValue(value);
		else
			throw new IllegalArgumentException("Bad " + targetClass.getSimpleName() + ": " + text);
	}

}
