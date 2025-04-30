package tr.com.eaaslan.library.util;

import tr.com.eaaslan.library.model.Book;
import tr.com.eaaslan.library.model.Genre;

import java.time.Year;
import java.util.List;

/**
 * Utility class containing hardcoded test data for Book entity tests
 */
public class BookTestData {

    private BookTestData() {
        // Private constructor to prevent instantiation
    }

    /**
     * Returns a list of hardcoded test books
     */
    public static List<Book> getTestBooks() {
        return List.of(
                // Book 1
                Book.builder()
                        .isbn("074322678X")
                        .title("Where You'll Find Me: And Other Stories")
                        .author("Ann Beattie")
                        .publicationYear(Year.of(2002))
                        .publisher("Scribner")
                        .genre(Genre.SCIENCE)
                        .imageUrl("http://images.amazon.com/images/P/074322678X.01.LZZZZZZZ.jpg")
                        .description("A collection of short stories")
                        .available(true)
                        .quantity(1)
                        .build(),

                // Book 2
                Book.builder()
                        .isbn("080652121X")
                        .title("Hitler's Secret Bankers")
                        .author("Adam Lebor")
                        .publicationYear(Year.of(2000))
                        .publisher("Citadel Press")
                        .genre(Genre.RELIGION)
                        .imageUrl("http://images.amazon.com/images/P/080652121X.01.LZZZZZZZ.jpg")
                        .description("The Myth of Swiss Neutrality During the Holocaust")
                        .available(true)
                        .quantity(1)
                        .build(),

                // Book 3
                Book.builder()
                        .isbn("1552041778")
                        .title("Jane Doe")
                        .author("R. J. Kaiser")
                        .publicationYear(Year.of(1999))
                        .publisher("Mira Books")
                        .genre(Genre.BUSINESS)
                        .imageUrl("http://images.amazon.com/images/P/1552041778.01.LZZZZZZZ.jpg")
                        .description("A thriller novel")
                        .available(true)
                        .quantity(1)
                        .build(),

                // Book 4
                Book.builder()
                        .isbn("1558746218")
                        .title("A Second Chicken Soup for the Woman's Soul")
                        .author("Jack Canfield")
                        .publicationYear(Year.of(1998))
                        .publisher("Health Communications")
                        .genre(Genre.POLITICS)
                        .imageUrl("http://images.amazon.com/images/P/1558746218.01.LZZZZZZZ.jpg")
                        .description("Chicken Soup for the Soul Series")
                        .available(true)
                        .quantity(1)
                        .build(),

                // Book 5
                Book.builder()
                        .isbn("1567407781")
                        .title("The Witchfinder")
                        .author("Loren D. Estleman")
                        .publicationYear(Year.of(1998))
                        .publisher("Brilliance Audio - Trade")
                        .genre(Genre.MUSIC)
                        .imageUrl("http://images.amazon.com/images/P/1567407781.01.LZZZZZZZ.jpg")
                        .description("Amos Walker Mystery Series")
                        .available(true)
                        .quantity(1)
                        .build(),

                // Book 6
                Book.builder()
                        .isbn("1575663937")
                        .title("More Cunning Than Man")
                        .author("Robert Hendrickson")
                        .publicationYear(Year.of(1999))
                        .publisher("Kensington Publishing Corp.")
                        .genre(Genre.AUTOBIOGRAPHY)
                        .imageUrl("http://images.amazon.com/images/P/1575663937.01.LZZZZZZZ.jpg")
                        .description("A Social History of Rats and Man")
                        .available(true)
                        .quantity(1)
                        .build(),

                // Book 7
                Book.builder()
                        .isbn("1881320189")
                        .title("Goodbye to the Buttermilk Sky")
                        .author("Julia Oliver")
                        .publicationYear(Year.of(1994))
                        .publisher("River City Pub")
                        .genre(Genre.HISTORY)
                        .imageUrl("http://images.amazon.com/images/P/1881320189.01.LZZZZZZZ.jpg")
                        .description("A historical novel")
                        .available(true)
                        .quantity(1)
                        .build(),

                // Book 8
                Book.builder()
                        .isbn("1841721522")
                        .title("New Vegetarian")
                        .author("Celia Brooks Brown")
                        .publicationYear(Year.of(2001))
                        .publisher("Ryland Peters & Small Ltd")
                        .genre(Genre.MEMOIR)
                        .imageUrl("http://images.amazon.com/images/P/1841721522.01.LZZZZZZZ.jpg")
                        .description("Bold and Beautiful Recipes for Every Occasion")
                        .available(true)
                        .quantity(1)
                        .build(),

                // Book 9
                Book.builder()
                        .isbn("1879384493")
                        .title("If I'd Known Then What I Know Now")
                        .author("J. R. Parrish")
                        .publicationYear(Year.of(2003))
                        .publisher("Cypress House")
                        .genre(Genre.DRAMA)
                        .imageUrl("http://images.amazon.com/images/P/1879384493.01.LZZZZZZZ.jpg")
                        .description("Why Not Learn from the Mistakes of Others?")
                        .available(true)
                        .quantity(1)
                        .build(),

                // Book 10
                Book.builder()
                        .isbn("3404921038")
                        .title("Wie Barney es sieht")
                        .author("Mordecai Richler")
                        .publicationYear(Year.of(2002))
                        .publisher("Lübbe")
                        .genre(Genre.PSYCHOLOGY)
                        .imageUrl("http://images.amazon.com/images/P/3404921038.01.LZZZZZZZ.jpg")
                        .description("German translation of Barney's Version")
                        .available(true)
                        .quantity(1)
                        .build(),

                // Book 11
                Book.builder()
                        .isbn("3442353866")
                        .title("Der Fluch der Kaiserin")
                        .author("Adam Lebor")
                        .publicationYear(Year.of(2001))
                        .publisher("Goldmann")
                        .genre(Genre.MUSIC)
                        .imageUrl("http://images.amazon.com/images/P/3442353866.01.LZZZZZZZ.jpg")
                        .description("Ein Richter-Di-Roman")
                        .available(false)
                        .quantity(0)
                        .build(),

                // Book 12
                Book.builder()
                        .isbn("3442410665")
                        .title("Sturmzeit. Roman.")
                        .author("Charlotte Link")
                        .publicationYear(Year.of(1991))
                        .publisher("Goldmann")
                        .genre(Genre.PSYCHOLOGY)
                        .imageUrl("http://images.amazon.com/images/P/3442410665.01.LZZZZZZZ.jpg")
                        .description("A German novel")
                        .available(false)
                        .quantity(0)
                        .build(),

                // Book 13
                Book.builder()
                        .isbn("3442446937")
                        .title("Tage der Unschuld.")
                        .author("Richard North Patterson")
                        .publicationYear(Year.of(2000))
                        .publisher("Goldmann")
                        .genre(Genre.TRAVEL)
                        .imageUrl("http://images.amazon.com/images/P/3442446937.01.LZZZZZZZ.jpg")
                        .description("German translation of Degree of Guilt")
                        .available(false)
                        .quantity(0)
                        .build(),

                // Book 14
                Book.builder()
                        .isbn("038078243X")
                        .title("Miss Zukas and the Raven's Dance")
                        .author("Jo Dereske")
                        .publicationYear(Year.of(1996))
                        .publisher("Avon")
                        .genre(Genre.PSYCHOLOGY)
                        .imageUrl("http://images.amazon.com/images/P/038078243X.01.LZZZZZZZ.jpg")
                        .description("A mystery novel")
                        .available(false)
                        .quantity(0)
                        .build(),

                // Book 15
                Book.builder()
                        .isbn("055321215X")
                        .title("Pride and Prejudice")
                        .author("Jane Austen")
                        .publicationYear(Year.of(1983))
                        .publisher("Bantam")
                        .genre(Genre.SELF_HELP)
                        .imageUrl("http://images.amazon.com/images/P/055321215X.01.LZZZZZZZ.jpg")
                        .description("A classic novel of manners")
                        .available(false)
                        .quantity(0)
                        .build(),

                // Book 16
                Book.builder()
                        .isbn("067176537X")
                        .title("The Therapeutic Touch")
                        .author("Dolores Krieger")
                        .publicationYear(Year.of(1979))
                        .publisher("Fireside")
                        .genre(Genre.POETRY)
                        .imageUrl("http://images.amazon.com/images/P/067176537X.01.LZZZZZZZ.jpg")
                        .description("How to Use Your Hands to Help or to Heal")
                        .available(true)
                        .quantity(1)
                        .build(),

                // Book 17
                Book.builder()
                        .isbn("042518630X")
                        .title("Purity in Death")
                        .author("Adam Lebor")
                        .publicationYear(Year.of(2002))
                        .publisher("Berkley Publishing Group")
                        .genre(Genre.CHILDREN)
                        .imageUrl("http://images.amazon.com/images/P/042518630X.01.LZZZZZZZ.jpg")
                        .description("A futuristic thriller")
                        .available(true)
                        .quantity(1)
                        .build(),

                // Book 18
                Book.builder()
                        .isbn("2070423204")
                        .title("Lieux dits")
                        .author("Michel Tournier")
                        .publicationYear(Year.of(2002))
                        .publisher("Gallimard")
                        .genre(Genre.OTHER)
                        .imageUrl("http://images.amazon.com/images/P/2070423204.01.LZZZZZZZ.jpg")
                        .description("French literature")
                        .available(true)
                        .quantity(1)
                        .build(),

                // Book 19
                Book.builder()
                        .isbn("042511774X")
                        .title("Breathing Lessons")
                        .author("Anne Tyler")
                        .publicationYear(Year.of(1994))
                        .publisher("Berkley Publishing Group")
                        .genre(Genre.DRAMA)
                        .imageUrl("http://images.amazon.com/images/P/042511774X.01.LZZZZZZZ.jpg")
                        .description("A Pulitzer Prize winning novel")
                        .available(true)
                        .quantity(1)
                        .build(),

                // Book 20
                Book.builder()
                        .isbn("1853262404")
                        .title("Heart of Darkness")
                        .author("Joseph Conrad")
                        .publicationYear(Year.of(1998))
                        .publisher("NTC/Contemporary Publishing Company")
                        .genre(Genre.MUSIC)
                        .imageUrl("http://images.amazon.com/images/P/1853262404.01.LZZZZZZZ.jpg")
                        .description("Wordsworth Collection")
                        .available(true)
                        .quantity(1)
                        .build()
        );
    }

    /**
     * Returns a subset of test books with specific genre
     */
    public static List<Book> getBooksWithGenre(Genre genre) {
        return getTestBooks().stream()
                .filter(book -> book.getGenre() == genre)
                .toList();
    }

    /**
     * Returns books that are available (quantity > 0)
     */
    public static List<Book> getAvailableBooks() {
        return getTestBooks().stream()
                .filter(Book::isAvailable)
                .toList();
    }
}