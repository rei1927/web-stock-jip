package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.User
import com.example.data.SalesTeam
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    viewModel: PropertyViewModel,
    onBack: () -> Unit
) {
    val allUsers by viewModel.allUsers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val allTeams by viewModel.allTeams.collectAsState()

    val role = currentUser?.role ?: ""
    val isSuperAdmin = role.contains("SUPER", ignoreCase = true)
    var selectedTab by remember(isSuperAdmin) { mutableStateOf(if (isSuperAdmin) 0 else 1) }

    var showAddUserDialog by remember { mutableStateOf(false) }
    var selectedUserForDelete by remember { mutableStateOf<User?>(null) }
    var selectedUserForEdit by remember { mutableStateOf<User?>(null) }

    var showAddTeamDialog by remember { mutableStateOf(false) }
    var showChangeLeaderDialogForTeam by remember { mutableStateOf<SalesTeam?>(null) }

    val tabs = listOf("Kelola Akun", "Pengaturan Tim")
    var userSearchQuery by remember { mutableStateOf("") }
    var roleFilter by remember { mutableStateOf<String?>(null) }

    val filteredUsers = remember(allUsers, userSearchQuery, roleFilter) {
        allUsers.filter { user ->
            (user.name.contains(userSearchQuery, ignoreCase = true) ||
             user.username.contains(userSearchQuery, ignoreCase = true)) &&
            (roleFilter == null || user.role == roleFilter)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Kelola Akun & Tim",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke Dashboard",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyDark)
            )
        },
        floatingActionButton = {
            if (isSuperAdmin && selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddUserDialog = true },
                    containerColor = GoldAccent,
                    contentColor = NavyDark,
                    modifier = Modifier.testTag("fab_add_user")
                ) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Tambah Akun")
                }
            } else if (selectedTab == 1) {
                FloatingActionButton(
                    onClick = { showAddTeamDialog = true },
                    containerColor = GoldAccent,
                    contentColor = NavyDark,
                    modifier = Modifier.testTag("fab_add_team")
                ) {
                    Icon(imageVector = Icons.Default.GroupAdd, contentDescription = "Tambah Tim Baru")
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftBackground)
                .padding(innerPadding)
        ) {
            if (isSuperAdmin) {
                // Tab Header
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = NavyPrimary
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        )
                    }
                }
            }

            if (selectedTab == 0) {
                // TAB 0: KELOLA AKUN
                // Header Info Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    border = BorderStroke(1.dp, BorderLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ORGANISASI AGEN TIM SALES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary,
                            letterSpacing = 1.0.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sebagai Admin / Super Admin, Anda berhak mendaftarkan akun baru, menghapus akun, dan menugaskan peran kepemimpinan anggota tim.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            lineHeight = 16.sp
                        )
                    }
                }

                // User accounts List
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    OutlinedTextField(
                        value = userSearchQuery,
                        onValueChange = { userSearchQuery = it },
                        placeholder = { Text("Cari Nama atau Username...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(null, "Sales", "Sales Manager", "Admin").forEach { role ->
                            FilterChip(
                                selected = roleFilter == role,
                                onClick = { roleFilter = if (roleFilter == role) null else role },
                                label = { Text(role ?: "Semua", fontSize = 10.sp) }
                            )
                        }
                    }
                }

                if (filteredUsers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (userSearchQuery.isEmpty()) "Tidak ada pengguna found" else "Hasil pencarian kosong",
                                color = ContentSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 88.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredUsers) { user ->
                            val isSelf = user.username == currentUser?.username
                            val roleColor = when (user.role) {
                                "Super Admin" -> Color(0xFFD32F2F)
                                "Admin" -> NavyPrimary
                                "Sales Manager" -> GoldAccent
                                else -> Color(0xFF2E7D32)
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, BorderLight),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(roleColor.copy(alpha = 0.1f), shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (user.role) {
                                                "Super Admin" -> Icons.Default.Security
                                                "Admin" -> Icons.Default.AdminPanelSettings
                                                "Sales Manager" -> Icons.Default.SupervisedUserCircle
                                                else -> Icons.Default.Person
                                            },
                                            contentDescription = null,
                                            tint = roleColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = user.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = ContentPrimary
                                            )
                                            if (isSelf) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .background(Color.Gray.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("AKUN ANDA", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = "Username: @${user.username} • PIN: ${user.pin}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )

                                        if (user.role == "Sales" && user.managerName != null) {
                                            Text(
                                                text = "Supervisor Manager: @${user.managerName}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = NavyPrimary,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }

                                    Row {
                                        IconButton(
                                            onClick = { selectedUserForEdit = user }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Anggota",
                                                tint = NavyPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        if (!isSelf) {
                                            IconButton(
                                                onClick = { selectedUserForDelete = user }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Hapus Anggota",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // TAB 1: PENGATURAN TIM (TEAM CONFIGURATION)
                val managersList = allUsers.filter { it.role == "Sales Manager" }
                val salesList = allUsers.filter { it.role == "Sales" }

                var showAddMemberDialogForTeam by remember { mutableStateOf<SalesTeam?>(null) }

                // Header Info Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    border = BorderStroke(1.dp, BorderLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "STRUKTUR TIM SALES & LEADER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary,
                            letterSpacing = 1.0.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Gunakan panel di bawah untuk mengelola tim sales, menambahkan tim baru, menghapus tim, menetapkan Sales Manager pimpinan, serta mengatur anggota di dalam tim tersebut.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            lineHeight = 16.sp
                        )
                    }
                }

                if (allTeams.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(72.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Belum Ada Tim Sales",
                                color = ContentPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Mulai kelola dengan menambahkan Tim baru melalui tombol '+' di kanan bawah.",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 88.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(allTeams) { team ->
                            val leader = allUsers.find { it.username == team.leaderUsername }
                            val teamMembers = if (team.leaderUsername != null) {
                                salesList.filter { it.managerName == team.leaderUsername }
                            } else {
                                emptyList()
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("team_card_${team.teamId}"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, BorderLight),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // Team Header
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(GoldAccent.copy(alpha = 0.15f), shape = CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Groups,
                                                    contentDescription = null,
                                                    tint = NavyDark,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = team.teamName.uppercase(),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp,
                                                    color = NavyDark
                                                )
                                                Text(
                                                    text = if (leader != null) {
                                                        "Leader: ${leader.name} (@${leader.username})"
                                                    } else {
                                                        "Leader: (Belum ditentukan)"
                                                    },
                                                    fontSize = 11.sp,
                                                    color = ContentSecondary
                                                )
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Ganti/Pilih Leader Button
                                            IconButton(
                                                onClick = { showChangeLeaderDialogForTeam = team },
                                                modifier = Modifier.size(32.dp).testTag("change_leader_${team.teamId}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Settings,
                                                    contentDescription = "Pilih / Ganti Leader",
                                                    tint = NavyPrimary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            if (team.leaderUsername != null) {
                                                IconButton(
                                                    onClick = { showAddMemberDialogForTeam = team },
                                                    modifier = Modifier.size(32.dp).testTag("add_member_to_${team.teamId}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PersonAdd,
                                                        contentDescription = "Tambah Anggota Tim",
                                                        tint = NavyPrimary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }

                                            IconButton(
                                                onClick = { viewModel.deleteTeam(team) },
                                                modifier = Modifier.size(32.dp).testTag("delete_team_${team.teamId}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Hapus Tim",
                                                    tint = TerjualRed,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Divider(color = BorderLight)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Team Members List
                                    Text(
                                        text = "ANGGOTA TIM SALES (${teamMembers.size})",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ContentSecondary,
                                        letterSpacing = 0.5.sp,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )

                                    if (team.leaderUsername == null) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(SoftBackground, shape = RoundedCornerShape(10.dp))
                                                .padding(12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Silakan tentukan Leader/Sales Manager terlebih dahulu untuk mengelola anggota sales di tim ini.",
                                                fontSize = 12.sp,
                                                color = Color.Gray,
                                                textAlign = TextAlign.Center,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    } else if (teamMembers.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(SoftBackground, shape = RoundedCornerShape(10.dp))
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Tim masih kosong. Klik tombol person+ di kanan atas untuk menambahkan sales ke tim ini.",
                                                fontSize = 12.sp,
                                                color = Color.Gray,
                                                textAlign = TextAlign.Center,
                                                lineHeight = 16.sp
                                            )
                                        }
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            teamMembers.forEach { member ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(SoftBackground.copy(alpha = 0.5f), shape = RoundedCornerShape(10.dp))
                                                        .border(1.dp, BorderLight.copy(alpha = 0.5f), shape = RoundedCornerShape(10.dp))
                                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.Person,
                                                            contentDescription = null,
                                                            tint = Color.Gray,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column {
                                                            Text(
                                                                text = member.name,
                                                                fontWeight = FontWeight.SemiBold,
                                                                fontSize = 13.sp,
                                                                color = ContentPrimary
                                                            )
                                                            Text(
                                                                text = "@${member.username}",
                                                                fontSize = 10.sp,
                                                                color = Color.Gray
                                                            )
                                                        }
                                                    }

                                                    IconButton(
                                                        onClick = {
                                                            viewModel.assignManagerToUser(member.username, null)
                                                        },
                                                        modifier = Modifier.size(32.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.PersonRemove,
                                                            contentDescription = "Hapus Anggota Tim",
                                                            tint = TerjualRed,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Dialog: Add member to team
                val activeAddMemberTeam = showAddMemberDialogForTeam
                if (activeAddMemberTeam != null) {
                    val team = activeAddMemberTeam
                    val leaderUsername = team.leaderUsername ?: ""
                    // List of salespersons eligible: either have no manager, or have another manager but we can move them
                    val eligibleSales = salesList.filter { it.managerName != leaderUsername }

                    AlertDialog(
                        onDismissRequest = { showAddMemberDialogForTeam = null },
                        title = {
                            Text(
                                text = "Tambah Anggota Tim ${team.teamName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = NavyDark
                            )
                        },
                        text = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 300.dp)
                            ) {
                                Text(
                                    text = "Pilih anggota sales untuk ditugaskan ke tim pimpinan @$leaderUsername:",
                                    fontSize = 12.sp,
                                    color = ContentSecondary,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                if (eligibleSales.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Seluruh sales sudah terdaftar di tim ini.",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        items(eligibleSales) { sales ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        viewModel.assignManagerToUser(sales.username, leaderUsername)
                                                        showAddMemberDialogForTeam = null
                                                    }
                                                    .background(SoftBackground, shape = RoundedCornerShape(8.dp))
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(
                                                        text = sales.name,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 13.sp,
                                                        color = ContentPrimary
                                                    )
                                                    Text(
                                                        text = if (sales.managerName == null) "Belum memiliki tim" else "Dari tim: @${sales.managerName}",
                                                        fontSize = 11.sp,
                                                        color = if (sales.managerName == null) GoldAccent else ContentSecondary
                                                    )
                                                }

                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Pilih",
                                                    tint = NavyPrimary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showAddMemberDialogForTeam = null }) {
                                Text("Tutup", color = NavyPrimary)
                            }
                        }
                    )
                }
            }
        }
    }

    // Modal: Confirmation delete user
    if (selectedUserForDelete != null) {
        val user = selectedUserForDelete!!
        AlertDialog(
            onDismissRequest = { selectedUserForDelete = null },
            title = { Text("Hapus Pengguna?", fontWeight = FontWeight.Bold, color = ContentPrimary) },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus akun @${user.username} (${user.name}) secara permanen dari basis data?",
                    color = ContentSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteUser(user)
                        selectedUserForDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedUserForDelete = null }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }

    // Modal: Edit user dialog
    if (selectedUserForEdit != null) {
        val user = selectedUserForEdit!!
        var nameInput by remember { mutableStateOf(user.name) }
        var pinInput by remember { mutableStateOf(user.pin) }
        var selectedRole by remember { mutableStateOf(user.role) }
        var selectedManagerLink by remember { mutableStateOf(user.managerName) }

        val managersList = allUsers.filter { it.role == "Sales Manager" }

        AlertDialog(
            onDismissRequest = { selectedUserForEdit = null },
            title = { Text("Edit Akun @${user.username}", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Nama Lengkap") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it },
                        label = { Text("PIN Keamanan") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Ubah Role", fontSize = 12.sp, color = ContentSecondary)
                    Column {
                        val roles = listOf("Sales", "Sales Manager", "Admin", "Super Admin")
                        roles.chunked(2).forEach { rowRoles ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                rowRoles.forEach { roleName ->
                                    FilterChip(
                                        selected = selectedRole == roleName,
                                        onClick = { selectedRole = roleName },
                                        label = { Text(roleName, fontSize = 11.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    if (selectedRole == "Sales" && managersList.isNotEmpty()) {
                        Text("Hubungkan ke Manager", fontSize = 12.sp, color = ContentSecondary)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            managersList.forEach { mgr ->
                                FilterChip(
                                    selected = selectedManagerLink == mgr.username,
                                    onClick = { selectedManagerLink = mgr.username },
                                    label = { Text(mgr.name.substringBefore(" "), fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addNewUser(user.username, nameInput, selectedRole, pinInput, selectedManagerLink)
                        selectedUserForEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Simpan Perubahan")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedUserForEdit = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Modal: Add user dialog
    if (showAddUserDialog) {
        var usernameInput by remember { mutableStateOf("") }
        var nameInput by remember { mutableStateOf("") }
        var pinInput by remember { mutableStateOf("") }
        var selectedRole by remember { mutableStateOf("Sales") }
        var selectedManagerLink by remember { mutableStateOf<String?>(null) }
        var errorString by remember { mutableStateOf<String?>(null) }

        val managersList = allUsers.filter { it.role == "Sales Manager" }

        AlertDialog(
            onDismissRequest = { showAddUserDialog = false },
            title = { Text("Daftarkan Akun Baru", fontWeight = FontWeight.Bold, color = NavyDark, fontSize = 18.sp) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (errorString != null) {
                        Text(errorString!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }

                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { usernameInput = it.trim().lowercase() },
                        label = { Text("Nama Pengguna (Username)") },
                        placeholder = { Text("e.g. siska, rudi") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Nama Lengkap (Display)") },
                        placeholder = { Text("e.g. Siska Lestari") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it },
                        label = { Text("Kode PIN Keamanan Login (Password)") },
                        placeholder = { Text("e.g. 1234") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Role Picker Column
                    Text("Pilih Hak Akses (Role)", fontSize = 12.sp, color = ContentSecondary)
                    Column {
                        val roles = listOf("Sales", "Sales Manager", "Admin", "Super Admin")
                        roles.chunked(2).forEach { rowRoles ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowRoles.forEach { roleName ->
                                    FilterChip(
                                        selected = selectedRole == roleName,
                                        onClick = {
                                            selectedRole = roleName
                                            if (roleName != "Sales") {
                                                selectedManagerLink = null
                                            }
                                        },
                                        label = { Text(roleName, fontSize = 11.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // Manager link dropdown (if role is Sales)
                    if (selectedRole == "Sales" && managersList.isNotEmpty()) {
                        Text("Hubungkan ke Penanggungjawab Manager", fontSize = 12.sp, color = ContentSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            managersList.forEach { mgr ->
                                FilterChip(
                                    selected = selectedManagerLink == mgr.username,
                                    onClick = { selectedManagerLink = mgr.username },
                                    label = { Text(mgr.name.substringBefore(" "), fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val isUserExists = allUsers.any { it.username == usernameInput }
                        if (usernameInput.isBlank() || nameInput.isBlank()) {
                            errorString = "Username dan Nama Lengkap tidak boleh kosong!"
                        } else if (isUserExists) {
                            errorString = "Username ini sudah terdaftar di basis data!"
                        } else {
                            viewModel.addNewUser(
                                username = usernameInput,
                                name = nameInput,
                                role = selectedRole,
                                pin = pinInput,
                                managerName = selectedManagerLink
                            )
                            showAddUserDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Daftarkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddUserDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }

    // Modal: Add Team Dialog
    if (showAddTeamDialog) {
        var teamNameInput by remember { mutableStateOf("") }
        var selectedLeaderForNewTeam by remember { mutableStateOf<String?>(null) }
        val managersList = allUsers.filter { it.role == "Sales Manager" }

        AlertDialog(
            onDismissRequest = { showAddTeamDialog = false },
            title = { Text("Tambah Tim Baru", fontWeight = FontWeight.Bold, color = NavyDark, fontSize = 18.sp) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = teamNameInput,
                        onValueChange = { teamNameInput = it },
                        label = { Text("Nama Tim") },
                        placeholder = { Text("e.g. Tim Garuda, Tim Elite") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Pilih Sales Manager Leader (Opsional):", fontSize = 12.sp, color = ContentSecondary)

                    if (managersList.isEmpty()) {
                        Text("Belum ada Sales Manager terdaftar. Silakan buat akun Sales Manager terlebih dahulu di tab Kelola Akun.", color = Color.Gray, fontSize = 12.sp)
                    } else {
                        Column {
                            managersList.forEach { mgr ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedLeaderForNewTeam = if (selectedLeaderForNewTeam == mgr.username) null else mgr.username
                                        }
                                        .background(
                                            if (selectedLeaderForNewTeam == mgr.username) GoldAccent.copy(alpha = 0.2f) else SoftBackground,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = mgr.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = ContentPrimary)
                                    if (selectedLeaderForNewTeam == mgr.username) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = "Terpilih", tint = NavyPrimary)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (teamNameInput.isNotBlank()) {
                            viewModel.addNewTeam(teamNameInput, selectedLeaderForNewTeam)
                            showAddTeamDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Tambah")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTeamDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }

    // Modal: Change Leader Dialog
    val activeChangeLeaderTeam = showChangeLeaderDialogForTeam
    if (activeChangeLeaderTeam != null) {
        val team = activeChangeLeaderTeam
        val managersList = allUsers.filter { it.role == "Sales Manager" }

        AlertDialog(
            onDismissRequest = { showChangeLeaderDialogForTeam = null },
            title = { Text("Pilih Sales Manager Tim", fontWeight = FontWeight.Bold, color = NavyDark, fontSize = 18.sp) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Pilih Sales Manager yang akan memimpin ${team.teamName}:", fontSize = 12.sp, color = ContentSecondary)

                    Button(
                        onClick = {
                            viewModel.assignLeaderToTeam(team.teamId, null)
                            showChangeLeaderDialogForTeam = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Kosongkan Leader", color = NavyDark)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    managersList.forEach { mgr ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.assignLeaderToTeam(team.teamId, mgr.username)
                                    showChangeLeaderDialogForTeam = null
                                }
                                .background(
                                    if (team.leaderUsername == mgr.username) GoldAccent.copy(alpha = 0.2f) else SoftBackground,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = mgr.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = ContentPrimary)
                            if (team.leaderUsername == mgr.username) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = "Terpilih", tint = NavyPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showChangeLeaderDialogForTeam = null }) {
                    Text("Tutup", color = NavyPrimary)
                }
            }
        )
    }
}
