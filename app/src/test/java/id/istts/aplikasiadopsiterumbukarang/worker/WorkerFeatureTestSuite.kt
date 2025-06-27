package id.istts.aplikasiadopsiterumbukarang.worker

import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker.EditProfileViewModelTest
import id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.worker.WorkerPlantingViewModelTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test Suite untuk menjalankan semua unit test yang berhubungan dengan fitur Worker.
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    EditProfileViewModelTest::class,
    WorkerPlantingViewModelTest::class,
    WorkerProfileViewModelTest::class
)
class WorkerFeatureTestSuite