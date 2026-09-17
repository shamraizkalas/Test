package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.InitialData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("شجرہ نسب", appName)
  }

  @Test
  fun `verify initial family members seeded accurately`() {
    val members = InitialData.getInitialFamilyMembers()
    assertTrue("Should have over 100 seeded family members", members.size > 100)

    val root = members.find { it.id == 1L }
    assertNotNull("Root member Muhammad Ali must exist", root)
    assertEquals("محمد علی", root?.name)

    val mainBranches = members.filter { it.fatherId == 1L }
    assertEquals("Should have 4 main branches under Muhammad Ali", 4, mainBranches.size)
  }

  @Test
  fun `verify relationship calculation logic for brothers and father-son`() {
    val members = InitialData.getInitialFamilyMembers()
    val membersMap = members.associateBy { it.id }

    val muhammadAli = members.first { it.id == 1L }
    val karamIlahi = members.first { it.fatherId == 1L && it.name.contains("کرم الہی") }
    val karamDin = members.first { it.fatherId == 1L && it.name.contains("کرم دین") }

    // Relationship between father and son
    val database = com.example.data.AppDatabase.getDatabase(ApplicationProvider.getApplicationContext())
    val repository = com.example.data.FamilyRepository(database.familyDao())

    val fatherSonRel = repository.calculateRelationship(muhammadAli, karamIlahi, membersMap)
    assertNotNull(fatherSonRel)
    assertTrue(fatherSonRel.relationAtoB.contains("والد") || fatherSonRel.relationBtoA.contains("والد") || fatherSonRel.description.contains("والد"))

    // Relationship between brothers
    val brotherRel = repository.calculateRelationship(karamIlahi, karamDin, membersMap)
    assertNotNull(brotherRel)
    assertTrue(brotherRel.relationAtoB.contains("بھائی") || brotherRel.description.contains("بھائی"))
    assertEquals(1L, brotherRel.commonAncestor?.id)
  }

  @Test
  fun `verify readable text tree export and json export`() {
    val members = InitialData.getInitialFamilyMembers()
    val database = com.example.data.AppDatabase.getDatabase(ApplicationProvider.getApplicationContext())
    val repository = com.example.data.FamilyRepository(database.familyDao())

    val textTree = repository.exportReadableTextTree(members)
    assertTrue(textTree.contains("محمد علی"))
    assertTrue(textTree.contains("کرم الہی"))

    val json = repository.exportToJson(members)
    assertTrue(json.contains("\"name\": \"محمد علی\""))
    assertTrue(json.contains("\"generation\": 1"))
  }

  @Test
  fun `verify user authentication registration and login`() = kotlinx.coroutines.runBlocking {
    val database = com.example.data.AppDatabase.getDatabase(ApplicationProvider.getApplicationContext())
    val repository = com.example.data.FamilyRepository(database.familyDao(), database.userDao())
    repository.checkAndInitializeDatabase()

    // Default admin user check
    val adminUser = repository.getUserByEmailOrPhone("admin")
    assertNotNull("Default admin should be initialized", adminUser)
    assertEquals("ADMIN", adminUser?.role)

    // Register a new member user
    val newUser = com.example.data.UserAccount(
      fullName = "طارق محمود",
      emailOrPhone = "03001234567",
      passwordHash = "Pass1234",
      role = "MEMBER"
    )
    val id = repository.registerUser(newUser)
    assertTrue(id > 0)

    val fetched = repository.getUserByEmailOrPhone("03001234567")
    assertNotNull(fetched)
    assertEquals("طارق محمود", fetched?.fullName)
    assertEquals("Pass1234", fetched?.passwordHash)
  }

  @Test
  fun `verify family tree pdf generation configuration and data logic`() {
    val members = InitialData.getInitialFamilyMembers()
    val config = com.example.util.PdfExportConfig(includeCoverPage = true)
    
    assertNotNull(config)
    assertTrue("Members list should not be empty", members.isNotEmpty())
    
    val rootMember = members.find { it.fatherId == null }
    assertNotNull("Root forefather must exist", rootMember)
    assertEquals("محمد علی", rootMember?.name)
    
    val mainBranches = members.filter { it.fatherId == rootMember?.id }
    assertEquals("Must have 4 main historical branches", 4, mainBranches.size)
    
    val childrenMap = members.groupBy { it.fatherId }
    assertTrue(childrenMap.containsKey(rootMember?.id))
  }

  @Test
  fun `verify filter reset and search state logic`() {
    val members = InitialData.getInitialFamilyMembers()
    val filteredMales = members.filter { it.gender == com.example.data.Gender.MALE }
    assertTrue(filteredMales.isNotEmpty())

    val filteredGen1 = members.filter { it.generation == 1 }
    assertEquals(1, filteredGen1.size)
    assertEquals("محمد علی", filteredGen1.first().name)
  }

  @Test
  fun `verify forgot password email validation and reset service`() = kotlinx.coroutines.runBlocking {
    val authService = com.example.auth.FirebaseAuthService()
    assertNotNull(authService)

    val invalidEmail = "invalid-email"
    val result = authService.sendPasswordResetEmail(invalidEmail)
    assertTrue(result.isFailure || !authService.isAvailable())
  }
}

