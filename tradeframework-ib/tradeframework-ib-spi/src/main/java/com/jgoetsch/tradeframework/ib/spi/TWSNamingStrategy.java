package com.jgoetsch.tradeframework.ib.spi;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;

import org.mapstruct.ap.spi.DefaultAccessorNamingStrategy;

/**
 * Custom naming strategy to recognize TWS object property accessors/mutators
 * with no prefixes.
 * @author jgoetsch
 */
public class TWSNamingStrategy extends DefaultAccessorNamingStrategy {
	private static final String twsPackage = "com.ib.client";

	@Override
	public boolean isGetterMethod(ExecutableElement method) {
		if (elementUtils.getPackageOf(method).getQualifiedName().contentEquals(twsPackage)) {
	        return method.getParameters().isEmpty()
	        		&& method.getReturnType().getKind() != TypeKind.VOID;
		}
		else
			return super.isGetterMethod(method);
	}

	@Override
	public boolean isSetterMethod(ExecutableElement method) {
		if (elementUtils.getPackageOf(method).getQualifiedName().contentEquals(twsPackage)) {
			return method.getReturnType().getKind() == TypeKind.VOID;
		}
		else
			return super.isSetterMethod(method);
	}

	@Override
	public String getPropertyName(ExecutableElement getterOrSetterMethod) {
		if (elementUtils.getPackageOf(getterOrSetterMethod).getQualifiedName().contentEquals(twsPackage)) {
			return getterOrSetterMethod.getSimpleName().toString();
		}
		else
			return super.getPropertyName(getterOrSetterMethod);
	}

}
