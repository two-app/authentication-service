package user

trait UserTestArbitraries {
  
  def arbitraryUser(): User = User(
    1, Option(2), Option(3), "First", "Last"
  )

}
