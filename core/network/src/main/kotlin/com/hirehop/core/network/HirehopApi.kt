package com.hirehop.core.network

import com.hirehop.core.network.dto.AnalysisRequest
import com.hirehop.core.network.dto.AnalysisResponse
import com.hirehop.core.network.dto.ConsentRequest
import com.hirehop.core.network.dto.ConsentResponse
import com.hirehop.core.network.dto.ContentReportRequest
import com.hirehop.core.network.dto.ContentReportResponse
import com.hirehop.core.network.dto.CoverLetterRequest
import com.hirehop.core.network.dto.CoverLetterResponse
import com.hirehop.core.network.dto.DeletionResponse
import com.hirehop.core.network.dto.MeResponse
import com.hirehop.core.network.dto.PacksResponse
import com.hirehop.core.network.dto.PrepQuestionsRequest
import com.hirehop.core.network.dto.PrepQuestionsResponse
import com.hirehop.core.network.dto.PurchaseRequest
import com.hirehop.core.network.dto.PurchaseResponse
import com.hirehop.core.network.dto.PurchasesResponse
import com.hirehop.core.network.dto.ResumeParseRequest
import com.hirehop.core.network.dto.ResumeParseResponse
import com.hirehop.core.network.dto.ServerExportResponse
import com.hirehop.core.network.dto.TailoringResponse
import com.hirehop.core.network.dto.TailoringStartRequest
import com.hirehop.core.network.dto.UnlockResponse
import com.hirehop.core.network.dto.WalletResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface HirehopApi {
    @GET("v1/me")
    suspend fun me(): MeResponse

    @POST("v1/me/consents")
    suspend fun recordConsent(@Body body: ConsentRequest): ConsentResponse

    @DELETE("v1/me")
    suspend fun deleteMe(): DeletionResponse

    @POST("v1/hirehop/resume/parse")
    suspend fun parseResume(@Body body: ResumeParseRequest): ResumeParseResponse

    @POST("v1/hirehop/analyses")
    suspend fun analyse(@Body body: AnalysisRequest): AnalysisResponse

    @POST("v1/hirehop/tailorings")
    suspend fun startTailoring(@Body body: TailoringStartRequest): TailoringResponse

    @GET("v1/hirehop/tailorings/{id}")
    suspend fun tailoring(@Path("id") id: String): TailoringResponse

    @POST("v1/hirehop/prep-questions")
    suspend fun prepQuestions(@Body body: PrepQuestionsRequest): PrepQuestionsResponse

    @POST("v1/hirehop/cover-letters")
    suspend fun coverLetter(@Body body: CoverLetterRequest): CoverLetterResponse

    @GET("v1/hirehop/wallet")
    suspend fun wallet(): WalletResponse

    @POST("v1/hirehop/applications/{applicationId}/unlock")
    suspend fun unlock(@Path("applicationId") applicationId: String): UnlockResponse

    @GET("v1/hirehop/packs")
    suspend fun packs(): PacksResponse

    @POST("v1/hirehop/purchases")
    suspend fun purchase(@Body body: PurchaseRequest): PurchaseResponse

    @GET("v1/hirehop/purchases")
    suspend fun purchases(): PurchasesResponse

    @POST("v1/hirehop/content-reports")
    suspend fun reportContent(@Body body: ContentReportRequest): ContentReportResponse

    @GET("v1/hirehop/me/export")
    suspend fun exportMe(): ServerExportResponse
}
