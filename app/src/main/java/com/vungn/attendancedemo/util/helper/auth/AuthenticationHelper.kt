package com.vungn.attendancedemo.util.helper.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import com.microsoft.graph.authentication.BaseAuthenticationProvider
import com.microsoft.identity.client.AcquireTokenSilentParameters
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAccount
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.IPublicClientApplication.ISingleAccountApplicationCreatedListener
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.ISingleAccountPublicClientApplication.SignOutCallback
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.SignInParameters
import com.microsoft.identity.client.exception.MsalException
import com.vungn.attendancedemo.R
import java.lang.Exception
import java.net.URL
import java.util.concurrent.CompletableFuture


// Singleton class - the app only needs a single instance
// of PublicClientApplication
class AuthenticationHelper(
    ctx: Context,
) : BaseAuthenticationProvider() {
    private var mPCA: ISingleAccountPublicClientApplication? = null
    private val mScopes = arrayOf("User.Read", "User.ReadBasic.All")
    private var mAccount: IAccount? = null

    private var initializationFuture: CompletableFuture<Unit> = CompletableFuture()

    init {
        PublicClientApplication.createSingleAccountPublicClientApplication(ctx,
            R.raw.auth_config_single_account,
            object : ISingleAccountApplicationCreatedListener {
                override fun onCreated(application: ISingleAccountPublicClientApplication) {
                    mPCA = application
                    initializationFuture.complete(Unit)
                }

                override fun onError(exception: MsalException) {
                    Log.e("AUTH_HELPER", "Error creating MSAL application", exception)
                    initializationFuture.completeExceptionally(exception)
                }
            })
    }

    private fun awaitInitialization() {
        initializationFuture.join()
    }

    // Other methods can call this before performing actions that require initialization
    private fun requireInitialization() {
        awaitInitialization()
        if (mPCA == null) {
            throw IllegalStateException("MSAL application not initialized")
        }
    }

    fun acquireTokenInteractively(activity: Activity?): CompletableFuture<IAuthenticationResult> {
        requireInitialization()
        val future = CompletableFuture<IAuthenticationResult>()
        val parameters =
            SignInParameters.builder().withActivity(activity !!).withScopes(listOf(*mScopes))
                .withCallback(getAuthenticationCallback(future)).build()
        mPCA?.signIn(parameters)
        return future
    }

    fun acquireTokenSilently(): CompletableFuture<IAuthenticationResult> {
        requireInitialization()
        val future = CompletableFuture<IAuthenticationResult>()
        val authority = mPCA?.configuration?.defaultAuthority?.authorityURL.toString()
        val parameters = AcquireTokenSilentParameters.Builder().fromAuthority(authority)
            .withScopes(mScopes.toList()).forAccount(mAccount)
            .withCallback(getAuthenticationCallback(future)).build()
        mPCA?.acquireTokenSilentAsync(parameters)
        return future
    }

    fun signOut() {
        mPCA?.signOut(object : SignOutCallback {
            override fun onSignOut() {
                Log.d("AUTH_HELPER", "Signed out")
            }

            override fun onError(exception: MsalException) {
                Log.d("AUTH_HELPER", "MSAL error signing out", exception)
            }
        })
    }

    fun loadAccount(callback: CurrentAccountCallback) {
        requireInitialization()
        if (mPCA == null) {
            return
        }
        mPCA?.getCurrentAccountAsync(object :
            ISingleAccountPublicClientApplication.CurrentAccountCallback {
            override fun onAccountLoaded(activeAccount: IAccount?) { // You can use the account data to update your UI or your app database.
                if (activeAccount != null) {
                    mAccount = activeAccount
                    callback.onAccountLoaded()
                } else {
                    callback.onError(Exception("No signed-in account"))
                }
            }

            override fun onAccountChanged(
                priorAccount: IAccount?, currentAccount: IAccount?
            ) {
                if (currentAccount == null) { // Perform a cleanup task as the signed-in account changed.
                    callback.onError(Exception("Current account null"))
                } else {
                    mAccount = currentAccount
                    callback.onAccountLoaded()
                }
            }

            override fun onError(exception: MsalException) {
                callback.onError(exception)
            }
        })
    }


    private fun getAuthenticationCallback(future: CompletableFuture<IAuthenticationResult>): AuthenticationCallback {
        return object : AuthenticationCallback {
            override fun onCancel() {
                future.cancel(true)
            }

            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                future.complete(authenticationResult)
            }

            override fun onError(exception: MsalException) {
                future.completeExceptionally(exception)
            }
        }
    }

    override fun getAuthorizationTokenAsync(requestUrl: URL): CompletableFuture<String> {
        return if (shouldAuthenticateRequestWithUrl(requestUrl)) {
            acquireTokenSilently().thenApply { obj: IAuthenticationResult -> obj.accessToken }
        } else CompletableFuture.completedFuture(null)
    }
}
