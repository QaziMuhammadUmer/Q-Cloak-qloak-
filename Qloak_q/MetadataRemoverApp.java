

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.ooxml.POIXMLProperties;
import org.apache.poi.ooxml.POIXMLProperties.CoreProperties;
import org.apache.poi.ooxml.POIXMLProperties.ExtendedProperties;
import org.apache.poi.ooxml.POIXMLProperties.CustomProperties;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

/**
 * MetadataRemoverApp
 *
 * This program can remove metadata from PDF, IMAGE, or DOCX files.
 * For JPEG/JPG images, it uses standard ImageIO to rewrite the image without metadata.
 * For other image types (PNG, GIF, BMP, TIFF), it uses Apache Commons Imaging.
 */
public class MetadataRemoverApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.print("Select file type (PDF/IMAGE/DOCX): ");
            String fileType = scanner.nextLine().trim().toUpperCase();

            System.out.print("Enter file path: ");
            String filePath = scanner.nextLine().trim();

            processFile(fileType, filePath);
            System.out.println("Metadata removed successfully!");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            // e.printStackTrace(); // Uncomment if you want full stack trace for debugging
        } finally {
            scanner.close();
        }
    }

    private static void processFile(String fileType, String filePath) throws Exception {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("File does not exist or is not a valid file");
        }

        switch (fileType) {
            case "PDF":
                removePdfMetadata(file);
                break;
            case "IMAGE":
                removeImageMetadata(file);
                break;
            case "DOCX":
                removeDocxMetadata(file);
                break;
            default:
                throw new IllegalArgumentException("Unsupported file type: " + fileType);
        }
    }

    private static void removePdfMetadata(File file) throws IOException {
        // Uses PDFBox 2.x: PDDocument.load(File) is available
        try (PDDocument document = PDDocument.load(file)) {
            // Remove all metadata (XMP and Info dictionary)
            document.getDocumentCatalog().setMetadata(null);
            document.getDocumentInformation().getCOSObject().clear(); 
            // Also clear the Document Information dictionary (Author, Title, etc.)

            String outputPath = getOutputPath(file.getAbsolutePath(), "_cleaned.pdf");
            document.save(outputPath);
            System.out.println("Saved cleaned PDF: " + outputPath);
        }
    }

    private static void removeImageMetadata(File file) throws Exception {
        // First, read the image into a BufferedImage (all metadata is ignored)
        BufferedImage image = Imaging.getBufferedImage(file);

        String extension = getFileExtension(file.getName()).toLowerCase();
        String outputPath = getOutputPath(file.getAbsolutePath(), "_cleaned" + extension);

        if (extension.equals(".jpg") || extension.equals(".jpeg")) {
            // For JPEG/JPG files, Apache Commons Imaging sometimes cannot write certain custom JPEG formats.
            // So we fall back to standard javax.imageio.ImageIO, which strips all metadata by default.
            boolean success = ImageIO.write(image, "JPEG", new File(outputPath));
            if (!success) {
                throw new IOException("ImageIO.write failed for JPEG output");
            }
            System.out.println("Saved cleaned JPEG image via ImageIO: " + outputPath);
        } else {
            // For other formats (PNG, GIF, BMP, TIFF), use Apache Commons Imaging:
            ImageFormats format = getImageFormat(extension);
            try {
                Imaging.writeImage(image, new File(outputPath), format);
                System.out.println("Saved cleaned image via Commons Imaging: " + outputPath);
            } catch (ImageWriteException e) {
                // If Commons Imaging still fails, you can optionally fall back to ImageIO as a last resort:
                String fallbackType = extension.substring(1).toUpperCase(); 
                // e.g., if ".png", fallbackType = "PNG"
                boolean wrote = ImageIO.write(image, fallbackType, new File(outputPath));
                if (!wrote) {
                    throw new IOException("Failed to write image as " + fallbackType + ": " + e.getMessage());
                }
                System.out.println("Saved cleaned image via ImageIO fallback (" + fallbackType + "): " + outputPath);
            }
        }
    }

    private static void removeDocxMetadata(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {

            POIXMLProperties props = document.getProperties();
            if (props != null) {
                // Remove Core Properties
                CoreProperties coreProps = props.getCoreProperties();
                if (coreProps != null) {
                    coreProps.setCategory("");
                    coreProps.setContentStatus("");
                    coreProps.setKeywords("");
                    coreProps.setLastModifiedByUser("");
                    coreProps.setRevision("");
                    coreProps.setVersion("");
                    coreProps.setTitle("");
                    coreProps.setSubjectProperty("");
                    coreProps.setDescription("");
                    coreProps.setIdentifier("");
                    // We omit setLanguage("") in case it is missing in some POI versions.
                }

                // Remove Extended Properties
                ExtendedProperties extendedProps = props.getExtendedProperties();
                if (extendedProps != null) {
                    extendedProps.setApplication("");
                    extendedProps.setAppVersion("");
                    extendedProps.setCompany("");
                    extendedProps.setManager("");
                    extendedProps.setTemplate("");
                }

                // Remove Custom Properties
                CustomProperties customProps = props.getCustomProperties();
                if (customProps != null) {
                    customProps.getUnderlyingProperties().setPropertyArray(null);
                }
            }

            String outputPath = getOutputPath(file.getAbsolutePath(), "_cleaned.docx");
            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                document.write(fos);
            }
            System.out.println("Saved cleaned DOCX: " + outputPath);
        }
    }

    private static String getOutputPath(String originalPath, String suffix) {
        int dotIndex = originalPath.lastIndexOf('.');
        if (dotIndex > 0) {
            return originalPath.substring(0, dotIndex) + suffix;
        }
        return originalPath + suffix;
    }

    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return fileName.substring(dotIndex);
        }
        return "";
    }

    private static ImageFormats getImageFormat(String extension) {
        switch (extension) {
            case ".png":
                return ImageFormats.PNG;
            case ".gif":
                return ImageFormats.GIF;
            case ".bmp":
                return ImageFormats.BMP;
            case ".tiff":
            case ".tif":
                return ImageFormats.TIFF;
            default:
                // For JPG/JPEG we already handle via ImageIO, so this default is only used if extension is missing:
                return ImageFormats.JPEG;
        }
    }
}
