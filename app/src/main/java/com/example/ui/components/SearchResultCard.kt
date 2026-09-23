package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.data.local.model.SearchResultItem

@Composable
fun SearchResultCard(
    item: SearchResultItem,
    queryKeywords: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("search_result_item_${item.rowid}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header: Badges & Law Short Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Badge Tipe Elemen
                    val badgeColor = when (item.tipeElemen) {
                        "AYAT" -> MaterialTheme.colorScheme.primaryContainer
                        "PENJELASAN" -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.tertiaryContainer
                    }
                    val badgeTextColor = when (item.tipeElemen) {
                        "AYAT" -> MaterialTheme.colorScheme.onPrimaryContainer
                        "PENJELASAN" -> MaterialTheme.colorScheme.onSecondaryContainer
                        else -> MaterialTheme.colorScheme.onTertiaryContainer
                    }

                    Surface(
                        color = badgeColor,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = item.tipeElemen,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = item.judulPeraturan,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Buka Pasal",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pasal & Ayat Title
            val articleDisplay = if (item.labelAyat.isNotBlank() && item.labelAyat != "Judul") {
                "${item.nomorPasal} - Ayat ${item.labelAyat}"
            } else {
                item.nomorPasal
            }

            Text(
                text = articleDisplay,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Chapter / Bab
            if (item.namaBab.isNotBlank()) {
                Text(
                    text = item.namaBab,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            // Highlighted Snippet
            val snippetText = item.matchedSnippet.ifBlank { item.konten.take(160) }
            val highlightColor = MaterialTheme.colorScheme.primary
            val highlightedString = androidx.compose.runtime.remember(snippetText, queryKeywords, highlightColor) {
                highlightKeywords(snippetText, queryKeywords, highlightColor)
            }

            Text(
                text = highlightedString,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2f
            )
        }
    }
}

/**
 * Highlighting kata kunci pencarian dalam teks hasil
 */
private fun highlightKeywords(
    text: String,
    keywords: List<String>,
    highlightColor: androidx.compose.ui.graphics.Color
) = buildAnnotatedString {
    val cleanKeywords = keywords.map { it.lowercase().trim() }.filter { it.isNotBlank() }
    if (cleanKeywords.isEmpty()) {
        append(text)
        return@buildAnnotatedString
    }

    val lowerText = text.lowercase()
    var currentIndex = 0

    while (currentIndex < text.length) {
        var closestMatchStart = -1
        var matchLength = 0

        for (kw in cleanKeywords) {
            val idx = lowerText.indexOf(kw, currentIndex)
            if (idx != -1 && (closestMatchStart == -1 || idx < closestMatchStart)) {
                closestMatchStart = idx
                matchLength = kw.length
            }
        }

        if (closestMatchStart != -1) {
            // Append teks sebelum match
            if (closestMatchStart > currentIndex) {
                append(text.substring(currentIndex, closestMatchStart))
            }
            // Append matched dengan style bold & background highlight
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.ExtraBold,
                    color = highlightColor
                )
            ) {
                append(text.substring(closestMatchStart, closestMatchStart + matchLength))
            }
            currentIndex = closestMatchStart + matchLength
        } else {
            append(text.substring(currentIndex))
            break
        }
    }
}
