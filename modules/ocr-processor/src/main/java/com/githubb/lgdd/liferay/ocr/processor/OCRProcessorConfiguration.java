package com.githubb.lgdd.liferay.ocr.processor;

import aQute.bnd.annotation.metatype.Meta;

@Meta.OCD(
    id = OCRProcessorConfiguration.PID,
    localization = "content/Language",
    name = "com.github.lgdd.liferay.ocr.processor.config-name"
)
public interface OCRProcessorConfiguration {

  @Meta.AD(
      deflt = "http://localhost:8000/detect-text",
      required = false,
      name = "com.github.lgdd.liferay.ocr.processor.ocr-endpoint-name",
      description = "com.github.lgdd.liferay.ocr.processor.ocr-endpoint-description"
  )
  String ocrEndpoint();

  String PID = "com.github.lgdd.liferay.ocr.processor.OCRProcessorConfiguration";
}
