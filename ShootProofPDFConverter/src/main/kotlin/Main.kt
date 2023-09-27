import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import java.io.*
fun main(args: Array<String>) {
    val pdfFile: File = File("C:\\Users\\ellio\\Downloads\\Input.pdf")
    val pdfText = GetPdfText(pdfFile)
    val csvContent = ProcessRawText(pdfText)
    WriteCsv(csvContent)
}

fun GetPdfText(pdfFile: File): List<String> {
    try {

        // Load the PDF document
        val document: PDDocument = Loader.loadPDF(pdfFile)

        // Create a PDFTextStripper object
        val pdfStripper = PDFTextStripper()

        // Extract text from the PDF
        val pdfText: String = pdfStripper.getText(document)

        // Print the extracted text
        //println(pdfText)

        // Close the document
        document.close()

        val pdfList = pdfText.split("\n").toMutableList()
        val headerLine = pdfList[0]
        var i = 0
        var end = false
        while(!end){
            var line = pdfList[i]
            if(line == headerLine){
                pdfList.removeAt(i)
                pdfList.removeAt(i)
            }
            i++
            if(i == pdfList.size)
                end = true
        }

        return pdfList
    } catch (e: Exception) {
        e.printStackTrace()
        return listOf("")
    }
}

fun ProcessRawText(pdfList: List<String>): List<Order>{
    val zipcodeRegex = Regex("[^,]+(,[^,]+)+(,[^,]+)+[\\d\\d\\d\\d\\d]")
    val sizeRegex = Regex("[2.5 x 3.5 (8 Wallets)]|[4 X 6]|[5 X 7]|[8 X 10]|[11 X 14]|[All Photos from this album]|[Single Photo]]")
    var orders = ArrayList<Order>()
    var i = 0
    var currentOrder: Order = Order(-1)
    while(i < (pdfList.size)){
        if(pdfList[i].startsWith("Order #")){
            val orderNum = Integer.valueOf(pdfList[i].substringAfter("#").replace("\r", "").plus(","))
            currentOrder = Order(orderNum)
            orders.add(currentOrder)
        }
        if(pdfList[i].startsWith("Gallery Name")){
            val galleryName = pdfList[i].substringAfter(":").replace("\r", "").plus(",")
            currentOrder.galleryName = galleryName
        }
        if(pdfList[i].startsWith("Billing Address")){
            val nameBillingAddress = pdfList[i+1].replace("\r", "").plus(",")
            currentOrder.nameBillingAddress = nameBillingAddress
        }
        if(zipcodeRegex.find(pdfList[i]) != null){
            i++
            var firstImage = pdfList[i].replace("\r", "")
            while(!pdfList[i].contains(".jpg")) {
                i++
                //if image name line does not contain photo type
                if (pdfList[i].contains(".jpg")) {
                    firstImage = firstImage.plus(pdfList[i]).substringBefore(".jpg").plus(".jpg,")
                    currentOrder.fileOrdered = firstImage
                }
            }
            //find size ordered
            var matchResult: MatchResult?
            do {
                i++
                matchResult = sizeRegex.find(pdfList[i])
            } while(matchResult == null)
            val matchedText = matchResult.value
            currentOrder.sizeOrdered = matchedText
            //find quantity and total price

            //get subsequent images
            var noMoreImages = false
            while(!noMoreImages){
                i+=6
                var subImage = pdfList[i].replace("\r", "")
                while(!pdfList[i].contains(".jpg")) {
                    i++
                    if(pdfList[i].endsWith(".jpg\r")){
                        subImage = subImage.plus(pdfList[i].replace("\r", ""))
                    }
                }

            }
        }
    }
    return orders
}

fun WriteCsv(csvContent: List<List<String>>) {
    try{
        val newCsv = File("C:\\Users\\ellio\\OneDrive\\Documents\\Output.csv")
        val fw = FileWriter(newCsv)
        val writer = BufferedWriter(fw)

        writer.write("Order#,GalleryName,NameBillingAddress,FilesOrdered,SizeOrdered,PackageCode,Quantity,Total Price,Package,\n")
        for(i in 0 until (csvContent.size)){
            for(j in 0 until (csvContent[i].size)) {
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