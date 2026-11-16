package com.hidayah.android.ui.screens.dua

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hidayah.android.state.HidayahViewModel
import com.hidayah.android.ui.components.*
import com.hidayah.android.ui.theme.*

private val pal: HidayahPalette @Composable get() = LocalHidayahPalette.current

@Composable
fun DuasScreen(viewModel: HidayahViewModel, onBack: () -> Unit) {
    val settings by viewModel.settings.collectAsState()
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("সব") }
    var favoritesOnly by remember { mutableStateOf(false) }
    var expandedId by remember { mutableIntStateOf(-1) }

    val duas = remember(viewModel) { viewModel.duas }
    val categories = remember(duas) { listOf("সব") + duas.map { it.category }.distinct() }

    val filtered = remember(duas, query, selectedCategory, favoritesOnly, settings.favoriteDuaIds) {
        duas.filter { dua ->
            val matchesQuery = query.isBlank() ||
                dua.title.contains(query, ignoreCase = true) ||
                dua.bengali.contains(query) ||
                dua.arabic.contains(query) ||
                dua.source.contains(query)
            val matchesCategory = selectedCategory == "সব" || dua.category == selectedCategory
            val matchesFavorite = !favoritesOnly || dua.id in settings.favoriteDuaIds
            matchesQuery && matchesCategory && matchesFavorite
        }
    }

    Column(Modifier.fillMaxSize().background(pal.background)) {
        EmeraldHeroHeader(
            title = "দোয়া ও যিকির",
            subtitle = "দৈনন্দিন জীবনের দোয়া",
            onBack = onBack
        ) {
            IconButton(onClick = { favoritesOnly = !favoritesOnly }) {
                Icon(
                    if (favoritesOnly) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "প্রিয় দুয়া",
                    tint = if (favoritesOnly) Gold else Cream
                )
            }
        }

        // ---------- Search ----------
        Column(Modifier.padding(horizontal = 16.dp).padding(top = 14.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("দুয়া খুঁজুন…", style = HidayahType.Body, color = pal.sub) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = pal.sub) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "মুছুন", tint = pal.sub)
                        }
                    }
                },
                singleLine = true,
                shape = HidayahShapes.Medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = pal.line,
                    focusedContainerColor = pal.card,
                    unfocusedContainerColor = pal.card
                )
            )
            Spacer(Modifier.height(10.dp))
            // ---------- Category chips ----------
            CategoryChips(categories, selectedCategory) { selectedCategory = it }
            Spacer(Modifier.height(4.dp))
        }

        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 6.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered, key = { it.id }) { dua ->
                DuaCard(
                    dua = dua,
                    expanded = expandedId == dua.id,
                    favorite = dua.id in settings.favoriteDuaIds,
                    onToggleExpand = { expandedId = if (expandedId == dua.id) -1 else dua.id },
                    onToggleFavorite = { viewModel.toggleFavoriteDua(dua.id) }
                )
            }
            if (filtered.isEmpty()) {
                item {
                    Text(
                        "কোনো দুয়া পাওয়া যায়নি",
                        style = HidayahType.Body, color = pal.sub,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryChips(categories: List<String>, selected: String, onSelect: (String) -> Unit) {
    androidx.compose.foundation.lazy.LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { cat ->
            SelectableChip(label = cat, selected = cat == selected, onClick = { onSelect(cat) })
        }
    }
}

@Composable
private fun DuaCard(
    dua: com.hidayah.android.data.dua.Dua,
    expanded: Boolean,
    favorite: Boolean,
    onToggleExpand: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        shape = HidayahShapes.Medium,
        colors = CardDefaults.cardColors(containerColor = pal.card),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxWidth().clickable(onClick = onToggleExpand).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = HidayahShapes.Pill, color = pal.brand.copy(alpha = 0.08f)) {
                            Text(dua.category, style = HidayahType.Caption.copy(fontSize = 10.sp), color = pal.brand,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(dua.source, style = HidayahType.Caption, color = pal.sub)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(dua.title, style = HidayahType.BodyMedium, color = pal.ink)
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "প্রিয়",
                        tint = if (favorite) androidx.compose.ui.graphics.Color(0xFFC0392B) else pal.sub
                    )
                }
                Icon(
                    if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null, tint = pal.sub
                )
            }
            AnimatedVisibility(expanded, enter = expandVertically(), exit = shrinkVertically()) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = pal.line)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        dua.arabic,
                        style = HidayahType.Arabic.copy(fontSize = 22.sp, lineHeight = 42.sp),
                        color = pal.brand,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(dua.bengali, style = HidayahType.Body, color = pal.ink)
                }
            }
        }
    }
}
