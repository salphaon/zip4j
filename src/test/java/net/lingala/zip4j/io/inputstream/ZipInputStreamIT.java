package net.lingala.zip4j.io.inputstream;

import net.lingala.zip4j.AbstractIT;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static net.lingala.zip4j.utils.TestUtils.getTestFileFromResources;
import static net.lingala.zip4j.utils.ZipFileVerifier.verifyFileContent;
import static org.assertj.core.api.Assertions.assertThat;

public class ZipInputStreamIT extends AbstractIT {

  @Test
  public void testExtractStoreWithoutEncryption() throws IOException {
    File createdZipFile = createZipFile(CompressionMethod.STORE);
    extractZipFileWithInputStreams(createdZipFile, null);
  }

  @Test
  public void testExtractStoreWithZipStandardEncryption() throws IOException {
    File createdZipFile = createZipFile(CompressionMethod.STORE, true, EncryptionMethod.ZIP_STANDARD, null, PASSWORD);
    extractZipFileWithInputStreams(createdZipFile, PASSWORD);
  }

  @Test
  public void testExtractStoreWithAesEncryption128() throws IOException {
    File createdZipFile = createZipFile(CompressionMethod.STORE, true, EncryptionMethod.AES, AesKeyStrength.KEY_STRENGTH_128, PASSWORD);
    extractZipFileWithInputStreams(createdZipFile, PASSWORD);
  }

  @Test
  public void testExtractStoreWithAesEncryption256() throws IOException {
    File createdZipFile = createZipFile(CompressionMethod.STORE, true, EncryptionMethod.AES, AesKeyStrength.KEY_STRENGTH_256, PASSWORD);
    extractZipFileWithInputStreams(createdZipFile, PASSWORD);
  }

  @Test
  public void testExtractDeflateWithoutEncryption() throws IOException {
    File createdZipFile = createZipFile(CompressionMethod.DEFLATE);
    extractZipFileWithInputStreams(createdZipFile, null);
  }

  @Test
  public void testExtractDeflateWithAesEncryption128() throws IOException {
    File createdZipFile = createZipFile(CompressionMethod.DEFLATE, true, EncryptionMethod.AES, AesKeyStrength.KEY_STRENGTH_128, PASSWORD);
    extractZipFileWithInputStreams(createdZipFile, PASSWORD);
  }

  @Test
  public void testExtractDeflateWithAesEncryption256() throws IOException {
    File createdZipFile = createZipFile(CompressionMethod.DEFLATE, true, EncryptionMethod.AES, AesKeyStrength.KEY_STRENGTH_256, PASSWORD);
    extractZipFileWithInputStreams(createdZipFile, PASSWORD);
  }

  private void extractZipFileWithInputStreams(File zipFile, char[] password) throws IOException {
    LocalFileHeader localFileHeader;
    int readLen;
    byte[] readBuffer = new byte[4096];
    int numberOfEntriesExtracted = 0;

    try (FileInputStream fileInputStream = new FileInputStream(zipFile)) {
      try (ZipInputStream zipInputStream = new ZipInputStream(fileInputStream, password)) {
        while ((localFileHeader = zipInputStream.getNextEntry()) != null) {
          File extractedFile = temporaryFolder.newFile(localFileHeader.getFileName());
          try (OutputStream outputStream = new FileOutputStream(extractedFile)) {
            while ((readLen = zipInputStream.read(readBuffer)) != -1) {
              outputStream.write(readBuffer, 0, readLen);
            }
          }
          verifyFileContent(getTestFileFromResources(localFileHeader.getFileName()), extractedFile);
          numberOfEntriesExtracted++;
        }
      }
    }

    assertThat(numberOfEntriesExtracted).isEqualTo(FILES_TO_ADD.size());
  }

  private File createZipFile(CompressionMethod compressionMethod) throws IOException {
    return createZipFile(compressionMethod, false, null, null, null);
  }

  private File createZipFile(CompressionMethod compressionMethod, boolean encryptFiles,
                             EncryptionMethod encryptionMethod, AesKeyStrength aesKeyStrength, char[] password)
      throws IOException {

    File outputFile = temporaryFolder.newFile("output.zip");
    deleteFileIfExists(outputFile);

    ZipFile zipFile = new ZipFile(outputFile, password);

    ZipParameters zipParameters = new ZipParameters();
    zipParameters.setCompressionMethod(compressionMethod);
    zipParameters.setEncryptFiles(encryptFiles);
    zipParameters.setEncryptionMethod(encryptionMethod);
    zipParameters.setAesKeyStrength(aesKeyStrength);

    zipFile.addFiles(AbstractIT.FILES_TO_ADD, zipParameters);

    return outputFile;
  }

  private void deleteFileIfExists(File file) {
    if (file.exists()) {
      if (!file.delete()) {
        throw new RuntimeException("Could not delete an existing zip file");
      }
    }
  }
}