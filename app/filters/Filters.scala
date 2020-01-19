package filters

import javax.inject.Inject
import play.api.http.DefaultHttpFilters
import play.filters.cors.CORSFilter
import play.filters.gzip.GzipFilter

/**
  * Created by yz on 2017/6/13.
  */

class Filters @Inject()(gzipFilter: GzipFilter, userFilter: UserFilter,corsFilter: CORSFilter) extends
  DefaultHttpFilters(gzipFilter, userFilter,corsFilter)
