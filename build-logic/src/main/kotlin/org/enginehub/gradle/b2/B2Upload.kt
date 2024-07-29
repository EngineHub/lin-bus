package org.enginehub.gradle.b2

import com.backblaze.b2.client.B2StorageClient
import com.backblaze.b2.client.B2StorageClientFactory
import com.backblaze.b2.client.contentSources.B2ContentTypes
import com.backblaze.b2.client.contentSources.B2FileContentSource
import com.backblaze.b2.client.structures.B2UploadFileRequest
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Upload cannot be cached")
abstract class B2Upload : DefaultTask() {
    /**
     * The directory to upload.
     */
    @get:InputDirectory
    abstract val inputDir: DirectoryProperty

    /**
     * The bucket to upload to.
     */
    @get:Input
    abstract val bucketName: Property<String>

    /**
     * The prefix to use for the files.
     */
    @get:Input
    abstract val prefix: Property<String>

    @TaskAction
    fun upload() {
        val client: B2StorageClient = B2StorageClientFactory.createDefaultFactory().create(
            "enginehub-b2-upload",
        )
        val bucketId = client.getBucketOrNullByName(bucketName.get())?.bucketId
            ?: error("Bucket ${bucketName.get()} not found")
        val prefixValue = prefix.get()
        inputDir.asFileTree.visit {
            if (isDirectory) {
                return@visit
            }
            logger.lifecycle("Uploading $path to $prefixValue/$path")
            client.uploadSmallFile(
                B2UploadFileRequest.builder(
                    bucketId,
                    "$prefixValue/$path",
                    B2ContentTypes.APPLICATION_OCTET,
                    B2FileContentSource.build(file)
                ).build()
            )
        }
    }
}
