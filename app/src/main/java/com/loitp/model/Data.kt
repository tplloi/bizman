package com.loitp.model

import android.util.Log
import androidx.annotation.Keep
import com.loitpcore.core.utilities.LDateUtil

@Keep
data class Data(
    val GST: Double,
    val GSTAmount: Double,
    val createdAt: String,
    val device: Device,
    val initPaymentResponse: InitPaymentResponse,
    val input: List<Input>,
    val isPaid: Boolean,
    val isTakeAway: Boolean,
    val items: List<Item>,
    val objectId: String,
    val paymentCallbackLog: PaymentCallbackLog,
    val paymentProvider: String,
    val resolved: Boolean,
    val resolvedBy: ResolvedBy,
    val shop: Shop,
    val status: String,
    val tableName: String,
    val customerName: String?,
    val total: Double,
    val transactionId: String,
    val updatedAt: String
) {

    private fun getCreateAt(): String? {
//        val time = LDateUtil.now() ?: ""
        val time = createdAt
        return LDateUtil.getDate(
            time,
            "dd-MM-yyyy HH:mm:ss"
        )
    }

    private fun getCustomerNameForPrint(): String {
        return customerName ?: "-"
    }

    companion object {
        private const val maxLengthPerLine = 24
        private const val spaceChar = " "
    }

    private fun getItem(): String {
//        var s = formatLine("SKU", "PRICE")
        var s = ""
        items.forEach { item ->
            s += "\n"
            s += formatCenter("------------------------")
            s += "\n"

            var name = "x${item.quantity} ${item.menuItem.title}"
            if (item.note.isEmpty()) {
                //do nothing
            } else {
                name += " (${item.note})"
            }
            if (name.length >= maxLengthPerLine) {
                s += formatLeft(name)
                s += "\n"
                s += formatLine("", "$${item.subTotal}")
            } else {
                s += formatLine(
                    name,
                    "$${item.subTotal}"
                )
            }
        }
//        if (items.isEmpty()) {
//            //do nothing
//        } else {
//            s += "\n"
//            s += formatCenter("------------------------")
//        }
        return s
    }

    private fun formatLeft(value: String): String {
//        val valueLength = value.length
//        val space = maxLengthPerLine - valueLength
//        val spaceHalf = space / 2
//        val formatValue: String
//        if (spaceHalf > 0) {
//            var spaceString = ""
//            for (i in 0..spaceHalf) {
//                spaceString += spaceChar
//            }
//            formatValue = spaceString + value + spaceString
//        } else {
//            formatValue = value
//        }
        return value
    }

    private fun formatCenter(value: String): String {
        val valueLength = value.length
        val space = maxLengthPerLine - valueLength
        val spaceHalf = space / 2
        val formatValue: String
        if (spaceHalf > 0) {
            var spaceString = ""
            for (i in 0..spaceHalf) {
                spaceString += spaceChar
            }
            formatValue = spaceString + value + spaceString
        } else {
            formatValue = value
        }
        return formatValue
    }

    private fun formatLine(key: String?, value: String?): String {
        val keyLength = (key ?: "").length
        val valueLength = (value ?: "").length
        val spaceLength = maxLengthPerLine - keyLength - valueLength
//        Log.e("_PRINTER", "$spaceLength = $maxLengthPerLine $keyLength $valueLength $key $value")
        val formatValue: String
        if (spaceLength >= 0) {
            var spaceString = ""
            for (i in 0 until spaceLength) {
                spaceString += spaceChar
            }
            formatValue = key + spaceString + value
        } else {
            formatValue = key + "\n" + value
        }
        return formatValue
    }

    //24 ki tu 1 hang ngang
    fun getPrintContent(): String {
        val content = """${formatCenter("Dikauri Bizman")}
${formatCenter("************************")}
${formatLeft("Order Id: $objectId")}
${formatLeft("Ordered Time: ${getCreateAt()}")}
${formatLeft("Device: ${device.name}")}
${formatLeft("Customer: ${getCustomerNameForPrint()}")}
${formatLeft("Table: $tableName")}
${formatLeft("Paid by: $paymentProvider")}
${formatLeft("Transaction Id: $transactionId")}
${formatCenter("")}
${formatCenter("************************")}
${getItem()}
${formatCenter("")}
${formatCenter("************************")}
${formatLine("ORDER VALUE:", "$${total}")}
"""
        Log.e("_PRINTER", content)
        return content
    }

}