import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import java.io.*

fun main(args: Array<String>) {
    val pdffile: File = File("C:\\Users\\ellio\\Downloads\\Input.pdf")
    OpenPDF(pdffile)
}

fun OpenPDF(pdffile: File) {
    try {

        // Load the PDF document
        val document: PDDocument = Loader.loadPDF(pdffile)

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