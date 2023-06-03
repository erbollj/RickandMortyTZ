package com.geektech.rickandmorty.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.geektech.rickandmorty.core.locationResultToLocationResultDomain
import com.geektech.rickandmorty.data.network.ApiService
import com.geektech.rickandmorty.domain.model.LocationResultDomain
import retrofit2.HttpException

class LocationPagingSource(private val api: ApiService):
    PagingSource<Int, LocationResultDomain>() {

    override fun getRefreshKey(state: PagingState<Int, LocationResultDomain>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchorPosition) ?: return null
        return page.prevKey?.minus(1) ?: return page.nextKey?.plus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LocationResultDomain> {
        val page = params.key ?: 1
        val response = api.getPagedLocations(page)
        val data = response.body()?.results?.locationResultToLocationResultDomain() ?: emptyList()

        if (response.isSuccessful) {
            val nextKey = page + 1
            val prevKey = if (page == 1) null else page - 1
            return LoadResult.Page(prevKey = prevKey, nextKey = nextKey, data = data)
        } else {
            return LoadResult.Error(HttpException(response))
        }
    }

}