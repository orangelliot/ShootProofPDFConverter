import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.*

fun main(args: Array<String>) {
    val fis: InputStream = File("C:\\Users\\ellio\\Downloads\\Input.pdf").inputStream()
    OpenPDF(fis)
}

fun OpenPDF(fis: InputStream) {
    try {

        // Load the PDF document
        val document = PDDocument()

        // Create a PDFTextStripper object
        val pdfStripper = PDFTextStripper()

        // Extract text from the PDF
        val pdfText: String = pdfStripper.getText(document)

        // Print the extracted text
        println(pdfText)

        // Close the document
        document.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}