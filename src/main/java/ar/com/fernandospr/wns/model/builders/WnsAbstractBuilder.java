package ar.com.fernandospr.wns.model.builders;

import java.util.ArrayList;
import java.util.List;

import ar.com.fernandospr.wns.model.WnsBinding;
import ar.com.fernandospr.wns.model.WnsImage;
import ar.com.fernandospr.wns.model.WnsText;
import ar.com.fernandospr.wns.model.WnsVisual;

public abstract class WnsAbstractBuilder<T extends WnsAbstractBuilder<T>> {
	protected abstract T getThis();

	protected abstract WnsVisual getVisual();

	protected abstract List<WnsBinding> getBindings();

	protected WnsBinding currentBinding;

	public T visualVersion(Integer version) {
		getVisual().version = version;
		return getThis();
	}

	public T visualLang(String lang) {
		getVisual().lang = lang;
		return getThis();
	}

	public T visualBaseUri(String baseUri) {
		getVisual().baseUri = baseUri;
		return getThis();
	}

	/**
	 * @param branding
	 *            should be any of
	 *            {@link ar.com.fernandospr.wns.model.types.WnsBrandingType}
	 */
	public T visualBranding(String branding) {
		getVisual().branding = branding;
		return getThis();
	}

	public T visualAddImageQuery(Boolean addImageQuery) {
		getVisual().addImageQuery = addImageQuery;
		return getThis();
	}

	public T newBinding() {
		currentBinding = new WnsBinding();
		return getThis();
	}

	public T addBinding() {
		if (currentBinding != null) {
			getBindings().add(currentBinding);
		}
		return getThis();
	}

	public T bindingFallback(String fallback) {
		currentBinding.fallback = fallback;
		return getThis();
	}

	public T bindingLang(String lang) {
		currentBinding.lang = lang;
		return getThis();
	}

	public T bindingBaseUri(String baseUri) {
		currentBinding.baseUri = baseUri;
		return getThis();
	}

	/**
	 * @param branding
	 *            should be any of
	 *            {@link ar.com.fernandospr.wns.model.types.WnsBrandingType}
	 */
	public T bindingBranding(String branding) {
		currentBinding.branding = branding;
		return getThis();
	}

	public T bindingContentId(String contentId) {
		currentBinding.contentId = contentId;
		return getThis();
	}

	public T bindingAddImageQuery(Boolean addImageQuery) {
		currentBinding.addImageQuery = addImageQuery;
		return getThis();
	}

	/**
	 * @param template
	 *            <p>
	 *            For tiles: should be any of
	 *            {@link ar.com.fernandospr.wns.model.types.WnsTileTemplate}
	 *            <p>
	 *            For toasts: should be any of
	 *            {@link ar.com.fernandospr.wns.model.types.WnsToastTemplate}
	 */
	protected T bindingTemplate(String template) {
		currentBinding.template = template;
		currentBinding.texts = null;
		currentBinding.images = null;
		return getThis();
	}

	protected T setBindingTextFields(String... textFields) {
		currentBinding.texts = new ArrayList<WnsText>();
		for (int i = 0; i < textFields.length; i++) {
			WnsText txt = new WnsText();
			txt.id = i + 1;
			txt.value = textFields[i] != null ? textFields[i] : "";
			currentBinding.texts.add(txt);
		}
		return getThis();
	}

	protected T setBindingImages(String... imgSrcs) {
		currentBinding.images = new ArrayList<WnsImage>();
		for (int i = 0; i < imgSrcs.length; i++) {
			WnsImage img = new WnsImage();
			img.id = i + 1;
			img.src = imgSrcs[i] != null ? imgSrcs[i] : "";
			currentBinding.images.add(img);
		}
		return getThis();
	}
}
