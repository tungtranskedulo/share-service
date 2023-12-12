package com.educative.coroutines

/*

class NetworkUserRepository(
    private val api: UserApi,
) : UserRepository {
    suspend fun getUser(): User = api.getUser().toDomainUser()
}

class NetworkNewsRepository(
    private val api: NewsApi,
    private val settings: SettingsRepository,
) : NewsRepository {

    suspend fun getNews(): List<News> = api.getNews()
        .map { it.toDomainNews() }

    suspend fun getNewsSummary(): List<News> {
        val type = settings.getNewsSummaryType()
        return api.getNewsSummary(type)
    }
}

class MainPresenter(
    private val view: MainView,
    private val userRepo: UserRepository,
    private val newsRepo: NewsRepository
) : BasePresenter {

    fun onCreate() {
        scope.launch {
            val user = userRepo.getUser()
            view.showUserData(user)
        }
        scope.launch {
            val news = async {
                newsRepo.getNews()
                    .sortedByDescending { it.date }
            }
            val newsSummary = async {
                newsRepo.getNewsSummary()
            }
            view.showNews(newsSummary.await(), news.await())
        }
    }
}

@Controller
class UserController(
    private val tokenService: TokenService,
    private val userService: UserService,
){
    @GetMapping("/me")
    suspend fun findUser(
        @PathVariable userId: String,
        @RequestHeader("Authorization") authorization: String
    ): UserJson {
        val userId = tokenService.readUserId(authorization)
        val user = userService.findUserById(userId)
        return user.toJson()
    }
}
*/
