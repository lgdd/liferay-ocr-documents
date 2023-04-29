# Liferay OCR Documents

Demonstrates how we can send images to an OCR service and add the detected text to the indexed document in Liferay DXP.

## How it works

This example is using a custom REST OCR service (https://github.com/lgdd/tess4j-rest) to detect the text in an image.

In Liferay DXP, we need a [custom module](modules/ocr-processor/) adding a `ModelContributor` for the `DLFileEntry`. So when a file is uploaded or modified, this custom component will do an HTTP call to the REST OCR service to send it the file. The HTTP response contains the detected text which is then added to the document to be indexed.

## How to use this demo

Run

```sh
docker-compose up --build
```

> **Note**: It will start Liferay DXP 7.4 (u73) and a custom REST OCR service (https://github.com/lgdd/tess4j-rest).

Once Liferay DXP is started:

- Verify in the logs that the module has started:

```
STARTED com.github.lgdd.liferay.ocr_x.x.x
```

- Login as admin (`test@liferay.com:test`)
- Navigate to `Site Administration > Content & Data > Documents and Media`.
- Drag'n'drop an image with text ([example here](https://github.com/lgdd/tess4j-rest/blob/main/src/test/resources/test-data/eurotext.png?raw=true))
- Go back to the home page (http://localhost:8080)
- Type a keyword from the image (with the image above, try `fox`)
- The uploaded image appears in the search results

## Feature Request

If you want to see an OCR feature natively available in Liferay DXP, feel free to vote, watch and comment this feature request: https://issues.liferay.com/browse/LPS-154849

## License

[MIT](LICENSE)
