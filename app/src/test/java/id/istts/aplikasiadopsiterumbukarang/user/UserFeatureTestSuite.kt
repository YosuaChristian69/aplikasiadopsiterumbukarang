package id.istts.aplikasiadopsiterumbukarang.user

import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.UserAddCoralViewModelTest
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.user.LocationSelectionViewModelTest
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.user.UserCoralDetailViewModelTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test Suite untuk menjalankan semua unit test yang berhubungan dengan fitur User
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    LocationSelectionViewModelTest::class,
    UserAddCoralViewModelTest::class,
    UserCoralDetailViewModelTest::class,
    UserPaymentViewModelTest::class,
    UserSelectSpeciesViewModelTest::class
)
class UserFeatureTestSuite