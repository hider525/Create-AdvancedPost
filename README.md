# Create: AdvancedPost

**Create: AdvancedPost** adds address-based parcel delivery and collection features to Create. The interface and chat messages are localized for English, Russian, and Ukrainian, following Create's warm brass-and-charcoal visual style.

Source: [github.com/hider525/Create-AdvancedPost](https://github.com/hider525/Create-AdvancedPost)

## Features

- **Packager address selection:** press the Packager button to open an address screen. Suggestions are read from unchecked entries on an attached Create Clipboard; addresses can also be entered manually.
- **Six-digit waybills:** each packed box gets a unique `ТТН: XXXXXX` label, and the player who initiated packing receives matching paper. Issued numbers are saved with the world and are not reused.
- **Parcel tracking:** use the waybill paper to view the destination, creation time and location, and whether the parcel has been collected. Collection by a parcel-station operator adds the recipient and collection location.
- **Large parcels:** boxes support up to 27 different item stacks.
- **Train parcel sorting:** attach a Create Clipboard to a non-depot train storage and list delivery addresses on unchecked lines. That storage only accepts matching packages. If no filtered storage matches, an unfiltered storage on the train can accept the package; if there is no unfiltered storage, it stays at the station.
- **Parcel stations:** seat a mob on any Create seat with inventory-bearing storage within four blocks to make it a station operator. Use a matching waybill on the operator to retrieve the parcel from nearby storage.
- **Special Frogport filters:**
  - `*+` forwards every package from its own buffer and the storage below to its configured target. It does not receive packages from the chain conveyor.
  - `*-` is the chain-conveyor fallback for packages without another matching destination. It only receives packages and moves buffered parcels into the storage below as space becomes available.
- **Languages:** English (`en_us`), Russian (`ru_ru`), and Ukrainian (`uk_ua`), selected automatically from Minecraft's language setting.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.259 or a compatible 21.1 release
- Create 6.0.10
- Java 21 to build

Install the same mod version on the client and server, along with NeoForge and Create.

## Build

Clone the repository and run the Gradle wrapper from the project directory:

```powershell
.\gradlew.bat build
```

On Linux or macOS:

```sh
./gradlew build
```

The distributable JAR is created in `build/libs/` as `Create-AdvancedPost-1.1.0.jar`.

## Usage

1. Attach a Create Clipboard to a Packager and put delivery addresses on unchecked lines. Press the Packager button and choose a suggestion or type an address.
2. For train sorting, attach a Clipboard to each storage you want to filter and add its accepted addresses on unchecked lines. Filtered storages only accept matching packages. A storage without a Clipboard acts as a fallback for packages that match none of the train's Clipboard filters. Depots are excluded.
3. To make a parcel station, place storage within four blocks of a Create seat and seat a mob on it. Use the matching waybill on the mob to collect a parcel from nearby storage.
4. Use the waybill paper to display the parcel's tracking information in chat.
5. Configure a Frogport's filter and target as usual. Use `*+` to forward all packages to its target, or `*-` as the unmatched-address receiver that unloads packages into storage below.

## Русский

### Возможности

- **Выбор адреса упаковщиком:** нажмите кнопку упаковщика, чтобы открыть экран адреса. Подсказки берутся из незачёркнутых записей прикреплённого планшета Create; адрес также можно ввести вручную.
- **Шестизначные ТТН:** каждая коробка получает уникальное имя `ТТН: XXXXXX`, а игрок, запустивший упаковку, получает бумагу с таким же номером. Выданные номера сохраняются в мире и не используются повторно.
- **Отслеживание посылок:** используйте бумагу с ТТН, чтобы посмотреть адрес, время и место создания посылки, а также узнать, получена ли она. При выдаче оператором отделения записываются получатель и место получения.
- **Большие посылки:** коробка вмещает до 27 разных стеков предметов.
- **Сортировка посылок в поезде:** прикрепите планшет Create к хранилищу поезда (кроме депо) и внесите адреса в незачёркнутые строки. Это хранилище принимает только посылки с подходящими адресами. Если ни один фильтрованный контейнер не подходит, посылку может принять хранилище без планшета; если такого хранилища нет, посылка остаётся на станции.
- **Отделения:** посадите моба на любое сиденье Create, рядом с которым (в радиусе четырёх блоков) есть хранилище с инвентарём. Используйте бумагу с соответствующей ТТН на мобе-операторе, чтобы получить посылку из ближайшего хранилища.
- **Специальные фильтры квакопортов:**
  - `*+` пересылает все посылки из собственного буфера и хранилища снизу на настроенную цель. Квакопорт не принимает посылки с цепного конвейера.
  - `*-` служит запасным адресом цепного конвейера для посылок без другого подходящего адресата. Он только принимает посылки и по мере появления места перемещает их из буфера в хранилище снизу.
- **Языки:** английский (`en_us`), русский (`ru_ru`) и украинский (`uk_ua`); язык выбирается настройкой Minecraft.

### Требования и установка

Minecraft 1.21.1, NeoForge 21.1.259 (или совместимая версия ветки 21.1), Create 6.0.10. Установите одинаковую версию мода на клиент и сервер вместе с NeoForge и Create.

Для сборки нужен Java 21. Из папки проекта выполните:

```powershell
.\gradlew.bat build
```

Готовый файл появится в `build/libs/` под именем `Create-AdvancedPost-1.1.0.jar`.

### Использование

1. Прикрепите планшет Create к упаковщику и внесите адреса в незачёркнутые строки. Нажмите кнопку упаковщика и выберите подсказку или введите адрес вручную.
2. Для сортировки в поезде прикрепите планшет к каждому хранилищу, которое нужно фильтровать, и внесите принимаемые адреса в незачёркнутые строки. Фильтрованные хранилища принимают только совпадающие посылки. Хранилище без планшета служит резервным для посылок, не совпавших ни с одним фильтром поезда. Депо не учитываются.
3. Для создания отделения поставьте хранилище в пределах четырёх блоков от сиденья Create и посадите на него моба. Используйте бумагу с ТТН на мобе, чтобы получить коробку с совпадающим номером из ближайшего хранилища.
4. Используйте бумагу с ТТН, чтобы вывести информацию об отслеживании в чат.
5. Настройте фильтр и цель квакопорта обычным способом. `*+` пересылает все посылки на цель, а `*-` принимает посылки без другого подходящего адреса и выгружает их в нижнее хранилище.

## Українська

### Можливості

- **Вибір адреси пакувальником:** натисніть кнопку пакувальника, щоб відкрити екран адреси. Підказки беруться з незакреслених записів прикріпленого планшета Create; адресу також можна ввести вручну.
- **Шестизначні ТТН:** кожна коробка отримує унікальну назву `ТТН: XXXXXX`, а гравець, який запустив пакування, отримує папір із таким самим номером. Видані номери зберігаються у світі й не використовуються повторно.
- **Відстеження посилок:** використайте папір із ТТН, щоб переглянути адресу, час і місце створення посилки та дізнатися, чи її вже отримали. Після видачі оператором відділення записуються отримувач і місце отримання.
- **Великі посилки:** коробка вміщує до 27 різних стеків предметів.
- **Відділення:** посадіть моба на будь-яке сидіння Create, поруч із яким (у радіусі чотирьох блоків) є сховище з інвентарем. Використайте папір із відповідною ТТН на мобі-операторі, щоб забрати посилку з найближчого сховища.
- **Сортування посилок у поїзді:** прикріпіть планшет Create до сховища поїзда (крім депо) та внесіть адреси в незакреслені рядки. Таке сховище приймає лише посилки з відповідними адресами. Якщо жоден фільтрований контейнер не підходить, посилку може прийняти сховище без планшета; якщо такого сховища немає, посилка залишається на станції.
- **Спеціальні фільтри квакопортів:**
  - `*+` пересилає всі посилки з власного буфера та сховища знизу до налаштованої цілі. Квакопорт не приймає посилки з ланцюгового конвеєра.
  - `*-` є резервною адресою ланцюгового конвеєра для посилок без іншого відповідного адресата. Він лише приймає посилки та, щойно з'являється місце, переміщує їх із буфера до сховища знизу.
- **Мови:** англійська (`en_us`), російська (`ru_ru`) та українська (`uk_ua`); мова вибирається налаштуванням Minecraft.

### Вимоги та встановлення

Minecraft 1.21.1, NeoForge 21.1.259 (або сумісна версія гілки 21.1), Create 6.0.10. Встановіть однакову версію мода на клієнт і сервер разом із NeoForge та Create.

Для збирання потрібна Java 21. У папці проєкту виконайте:

```powershell
.\gradlew.bat build
```

Готовий файл з'явиться в `build/libs/` під назвою `Create-AdvancedPost-1.1.0.jar`.

### Використання

1. Прикріпіть планшет Create до пакувальника та додайте адреси в незакреслені рядки. Натисніть кнопку пакувальника й виберіть підказку або введіть адресу вручну.
2. Для сортування в поїзді прикріпіть планшет до кожного сховища, яке потрібно фільтрувати, та внесіть прийнятні адреси в незакреслені рядки. Фільтровані сховища приймають лише посилки з відповідними адресами. Сховище без планшета є резервним для посилок, що не відповідають жодному фільтру поїзда. Депо не враховуються.
3. Щоб створити відділення, поставте сховище в межах чотирьох блоків від сидіння Create та посадіть на нього моба. Використайте папір із ТТН на мобі, щоб отримати коробку з відповідним номером із найближчого сховища.
4. Використайте папір із ТТН, щоб переглянути інформацію про відстеження в чаті.
5. Налаштуйте фільтр і ціль квакопорта звичайним способом. `*+` пересилає всі посилки до цілі, а `*-` приймає посилки без іншої відповідної адреси та вивантажує їх у нижнє сховище.
