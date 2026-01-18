#include <iostream>
#include <string>
#include <limits> // Required for numeric_limits
#include <cstdlib> // For system()

// Function to display a simple "background" (clears screen and prints a title)
void displayBackground(const std::string& title) {
    // Clear the console screen (platform-specific)
#ifdef _WIN32
    std::system("cls"); // For Windows
#else
    std::system("clear"); // For Linux/macOS
#endif
    if (!title.empty()) {
        std::cout << title << "\n";
    }
}

// Function to handle account creation
void createAccount() {
    std::string username;
    std::string password;
    std::string confirmPassword;
    int age;

    displayBackground("Create New Account");

    std::cout << "Enter username: ";
    std::cin >> std::ws;
    std::getline(std::cin, username);

    std::cout << "Enter password: ";
    std::cin >> std::ws;
    std::getline(std::cin, password);

    std::cout << "Confirm password: ";
    std::cin >> std::ws;
    std::getline(std::cin, confirmPassword);

    if (password != confirmPassword) {
        std::cout << "\nError: Passwords do not match. Please try again." << std::endl;
        return; // don't let user proceed if passwords don't match
    }

    std::cout << "Enter your age: ";
    while (!(std::cin >> age) || age < 0) {
        std::cout << "Invalid input. Please enter a non-negative integer for age: ";
        std::cin.clear(); // Clear error flags
        std::cin.ignore(std::numeric_limits<std::streamsize>::max(), '\n'); // Discard invalid input
    }
    std::cin.ignore(std::numeric_limits<std::streamsize>::max(), '\n'); // Consume the remaining newline character

    // In a real application, you would save this data to a file or database
    std::cout << "\nAccount created successfully!" << std::endl;
    std::cout << "Username: " << username << std::endl;
    std::cout << "Age: " << age << std::endl;
}

int main() {
    createAccount();

    std::cout << "\nPress Enter to continue...";
    std::string _tmp;
    std::getline(std::cin, _tmp);

    return 0;
}