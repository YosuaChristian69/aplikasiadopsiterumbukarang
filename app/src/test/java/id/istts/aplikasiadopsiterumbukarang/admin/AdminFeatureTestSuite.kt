package id.istts.aplikasiadopsiterumbukarang.admin

import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.AddCoralViewModelTest
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.editUser.EditUserViewModelTest
import id.istts.aplikasiadopsiterumbukarang.usecases.AddCoralUseCaseTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite::class)
@Suite.SuiteClasses(
    AddCoralUseCaseTest::class,
    AddCoralViewModelTest::class,
    AddWorkerViewModelTest::class,
    AdminDashboardViewModelTest::class,
    AdminWorkerDashboardViewModelTest::class,
    EditCoralViewModelTest::class,
    EditUserViewModelTest::class
)
class AdminFeatureTestSuite