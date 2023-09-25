import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import java.io.*

val packageCodes = mapOf("2.5 x 3.5 (8 Wallets)" to 'A', "4 X 6" to 'B', "5 X 7" to 'C', "8 X 10" to 'D', "11 X 14" to 'E', "All Photos from this album" to 'F', "Single Photo" to 'G')
fun main(args: Array<String>) {
    val pdfFile: File = File("C:\\Users\\ellio\\Downloads\\Input.pdf")
    val pdfText = GetPdfText(pdfFile)
    val csvContent = ProcessRawText(pdfText)
    WriteCsv(csvContent)
}

fun GetPdfText(pdfFile: File): String {
    try {

        // Load the PDF document
        val document: PDDocument = Loader.loadPDF(pdfFile)

        // Create a PDFTextStripper object
        val pdfStripper = PDFTextStripper()

        //val pdfImages = PDF`

        // Extract text from the PDF
        val pdfText: String = pdfStripper.getText(document)

        // Print the extracted text
        println(pdfText)

        // Close the document
        document.close()

        return pdfText
    } catch (e: Exception) {
        e.printStackTrace()
        return ""
    }
}

fun ProcessRawText(pdfText: String): List<List<String>>{
    var csvContent = ArrayList<ArrayList<String>>()
    val pdfList = pdfText.split("\n")
    var atEnd = false
    var j = -1
    for(i in 0..(pdfList.size-1)){
        var line = pdfList[i]
        if(line.startsWith("Order #")){
            val orderNum = line.substringAfter("#").replace("\r", "").plus(",")
            csvContent.add(ArrayList<String>())
            j += 1
            csvContent[j].add(orderNum)
        }
        if(line.startsWith("Gallery Name")){
            val galleryName = line.substringAfter(":").replace("\r", "").plus(",")
            csvContent[j].add(galleryName)
        }
        if(line.startsWith("Billing Address")){
            val nameBillingAddress = pdfList[i+1].replace("\r", "").plus(",")
        }

    }
    return csvContent
}

fun WriteCsv(csvContent: List<List<String>>) {
    try{
        val newCsv = File("C:\\Users\\ellio\\OneDrive\\Documents\\Output.csv")
        val fw = FileWriter(newCsv)
        val writer = BufferedWriter(fw)

        writer.write("Order#,GalleryName,NameBillingAddress,FilesOrdered,SizeOrdered,PackageCode,Quantity,Total Price,Package,\n")
        for(i in 0..(csvContent.size-1)){
            for(j in 0..(csvContent[i].size-1)) {
                writer.write(csvContent[i][j])
            }
            writer.write("\n")
        }
        writer.close()
    }
    catch(ioe: IOException){
        ioe.printStackTrace()
    }
}