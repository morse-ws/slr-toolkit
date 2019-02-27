/*
 * generated by Xtext
 */
package de.tudresden.slr.model.taxonomy.ui;

import org.eclipse.xtext.ui.guice.AbstractGuiceAwareExecutableExtensionFactory;
import org.osgi.framework.Bundle;

import com.google.inject.Injector;

import de.tudresden.slr.model.taxonomy.ui.internal.TaxonomyActivator;

/**
 * This class was generated. Customizations should only happen in a newly
 * introduced subclass. 
 */
public class TaxonomyExecutableExtensionFactory extends AbstractGuiceAwareExecutableExtensionFactory {

	@Override
	protected Bundle getBundle() {
		return TaxonomyActivator.getInstance().getBundle();
	}
	
	@Override
	protected Injector getInjector() {
		return TaxonomyActivator.getInstance().getInjector(TaxonomyActivator.DE_TUDRESDEN_SLR_MODEL_TAXONOMY_TAXONOMY);
	}
	
}
