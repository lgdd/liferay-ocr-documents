package com.githubb.lgdd.liferay.ocr.processor;

import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Localization;
import com.liferay.portal.search.spi.model.index.contributor.ModelDocumentContributor;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

@Component(
    immediate = true,
    configurationPid = OCRProcessorConfiguration.PID,
    property = "indexer.class.name=com.liferay.document.library.kernel.model.DLFileEntry",
    service = ModelDocumentContributor.class
)
public class DLFileEntryModelContributor implements ModelDocumentContributor<DLFileEntry> {

  @Override
  public void contribute(Document document, DLFileEntry fileEntry) {
    _log.info(fileEntry.getFileName());
    if (!fileEntry.isInTrash() && fileEntry.getMimeType().contains("image")) {
      _doOCR(fileEntry, document);
    }
  }

  private void _doOCR(DLFileEntry fileEntry, Document document) {
    try {
      _log.info("Running OCR for '" + fileEntry.getFileName() + "'...");
      final MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
      multipartEntityBuilder.addBinaryBody(
          "file",
          fileEntry.getContentStream(),
          ContentType.MULTIPART_FORM_DATA,
          fileEntry.getFileName());
      final HttpEntity httpEntity = multipartEntityBuilder.build();
      final HttpPost httpPost = new HttpPost(_config.ocrEndpoint());
      httpPost.setEntity(httpEntity);
      try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        String result = EntityUtils.toString(httpResponse.getEntity());
        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
          _addDetectedText(fileEntry, document, result);
          _log.info("Detected text for '" + fileEntry.getFileName() + "': " + result);
        } else {
          _log.error("HTTP " + httpResponse.getStatusLine().getStatusCode() + ": " + result);
        }
      }
    } catch (PortalException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void _addDetectedText(DLFileEntry fileEntry, Document document, String detectedText) {
    for (Locale locale : _language.getAvailableLocales(fileEntry.getGroupId())) {
      String languageId = LocaleUtil.toLanguageId(locale);
      document.addText(_localization.getLocalizedName(Field.CONTENT, languageId), detectedText);
    }
  }

  @Activate
  @Modified
  public void activate(Map<String, String> properties) {
    _config = ConfigurableUtil.createConfigurable(OCRProcessorConfiguration.class, properties);
  }

  @Reference
  private Localization _localization;

  @Reference
  private Language _language;

  private volatile OCRProcessorConfiguration _config;

  private static final Log _log = LogFactoryUtil.getLog(DLFileEntryModelContributor.class);
}
