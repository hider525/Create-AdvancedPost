# Create: AdvancedPost

Create: AdvancedPost adds postal workflows to Create: choose a delivery address, label packages with unique waybills, route parcels through train networks, and retrieve them from staffed parcel stations.

**Current version: 1.1.3**

Minecraft **1.21.1** · NeoForge **21.1.259** · Create **6.0.10**

[Source code](https://github.com/hider525/Create-AdvancedPost)

## Features

- **Two-step Packager screen:** first enter or select the delivery address from suggestions read from unchecked entries on an attached Create Clipboard. Select **Next** to continue.
- **Optional recipient:** on the second screen, enter a player name or select an online player from suggestions. The sender is not listed. Leave the field empty to send without assigning a recipient.
- **Waybills for sender and recipient:** every packed box gets a unique six-digit `Waybill: XXXXXX` name. The sender receives matching paper; when a recipient is specified, they receive paper with the same number too.
- **Offline recipient support:** a known recipient who is offline receives their waybill paper the next time they join. The pending waybill is saved with world data.
- **Persistent unique numbers:** issued waybill numbers are stored with the world and are not reused.
- **Larger packages:** packages can hold up to 27 different item types instead of Create's default 9.
- **Train parcel sorting:** attach a Create Clipboard to non-depot train storage and list accepted addresses on unchecked lines. Filtered storage accepts matching packages; storage without a Clipboard is the fallback for unmatched packages. If no fallback storage exists, the package remains at the station.
- **Mob-operated parcel stations:** seat a mob on a Create seat with inventory-bearing storage within four blocks. Use a matching waybill on the mob to retrieve the package from nearby storage.
- **Special Frogport filters:**
  - `*+` forwards packages from the Frogport buffer and supported storage below (including Create storage) to its configured target. It does not receive packages from the chain conveyor.
  - `*-` receives packages for which the chain-conveyor network has no other matching destination. It unloads its buffer into storage below as space becomes available.
- **Localized interface and messages:** English, Russian, and Ukrainian, following the Minecraft language setting.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.259 or a compatible 21.1 release
- Create 6.0.10

Install the same mod version on the client and server.

## Usage

1. Attach a Create Clipboard to the Packager and add delivery addresses as unchecked entries.
2. Press the Packager button. Choose an address suggestion or enter one manually, then select **Next**.
3. On the recipient screen, select an online player or enter a known player name. Leave it blank if no recipient is needed, then select **Send**.
4. Configure the receiving Create package port to accept the selected delivery address. The sender receives a paper waybill; a specified recipient receives a matching copy, including on their next login if they are offline.
5. For train sorting, attach a Clipboard to each non-depot storage to filter and list accepted addresses on unchecked lines. A storage without a Clipboard is the fallback for unmatched packages.
6. To set up a parcel station, place inventory-bearing storage within four blocks of a Create seat and seat a mob on it. Use the matching waybill on the mob to collect the package.
7. Configure a Frogport's target as usual. Use `*+` to forward packages, or `*-` as the chain-conveyor fallback receiver.

## Building

Java 21 is required. From the project directory run:

```powershell
.\gradlew.bat build
```

On Linux or macOS:

```sh
./gradlew build
```

The distributable JAR is created in `build/libs/` as `Create-AdvancedPost-1.1.3.jar`.

## Русский

Create: AdvancedPost добавляет в логистику Create почтовые адреса, ТТН, сортировку посылок в поездах и выдачу посылок через отделения с мобами-операторами.

### Возможности

- **Два шага упаковщика:** сначала выберите адрес доставки из подсказок прикреплённого планшета Create или введите его вручную. Используются незачёркнутые записи. Нажмите **«Далее»**.
- **Получатель — необязательно:** на втором экране введите ник или выберите игрока из списка онлайн-игроков. Отправитель в списке не показывается. Поле можно оставить пустым.
- **ТТН для отправителя и получателя:** коробка получает уникальный шестизначный номер `ТТН: XXXXXX`. Отправитель получает бумагу с этим номером; если указан получатель, он также получит бумагу с той же ТТН.
- **Получатель офлайн:** известный серверу игрок получит свою бумагу при следующем входе. Ожидающая выдача сохраняется в данных мира.
- **Уникальные номера:** выданные ТТН сохраняются в мире и не используются повторно.
- **Большие посылки:** до 27 разных типов предметов вместо стандартных для Create 9.
- **Сортировка в поездах:** прикрепите планшет к хранилищу поезда (кроме депо) и внесите принимаемые адреса в незачёркнутые строки. Хранилище без планшета служит резервным для посылок, не подошедших под фильтры; без него посылка остаётся на станции.
- **Отделения с мобом-оператором:** посадите моба на сиденье Create, если в пределах четырёх блоков есть хранилище с инвентарём. Используйте бумагу с совпадающей ТТН на мобе, чтобы забрать посылку.
- **Особые фильтры квакопортов:** `*+` пересылает посылки из буфера и доступных хранилищ под квакопортом, включая хранилища Create, на настроенную цель, а `*-` принимает посылки без другого подходящего адреса в сети цепных конвейеров и выгружает их в нижнее хранилище.
- **Языки:** английский, русский и украинский; язык интерфейса определяется настройкой Minecraft.

### Требования и использование

Нужны Minecraft 1.21.1, NeoForge 21.1.259 (или совместимая версия ветки 21.1) и Create 6.0.10. Установите одинаковую версию мода на клиент и сервер.

1. Прикрепите планшет к упаковщику и внесите адреса в незачёркнутые строки.
2. Нажмите кнопку упаковщика, выберите адрес и нажмите **«Далее»**.
3. Укажите получателя или оставьте поле пустым, затем отправьте посылку. Бумага с ТТН выдаётся отправителю и, если получатель указан, ему. Офлайн-игрок получит её при входе.
4. Для сортировки в поезде прикрепите планшеты к фильтруемым хранилищам; хранилище без планшета будет резервным. Депо не учитываются.
5. Для отделения поставьте хранилище с инвентарём в пределах четырёх блоков от сиденья Create и посадите на него моба. Используйте соответствующую ТТН на мобе.
6. Настройте цель квакопорта обычным способом: `*+` пересылает посылки, `*-` принимает посылки без другого подходящего адреса.

## Українська

Create: AdvancedPost додає до логістики Create поштові адреси, ТТН, сортування посилок у поїздах і видачу посилок у відділеннях із мобами-операторами.

### Можливості

- **Два кроки пакувальника:** спочатку виберіть адресу доставки з підказок прикріпленого планшета Create або введіть її вручну. Використовуються незакреслені записи. Натисніть **«Далі»**.
- **Отримувач необов’язковий:** на другому екрані введіть нік або виберіть гравця зі списку онлайн-гравців. Відправник у списку відсутній. Поле можна залишити порожнім.
- **ТТН для відправника й отримувача:** коробка отримує унікальний шестизначний номер `ТТН: XXXXXX`. Відправник отримує папір із цим номером; якщо отримувача вказано, він також отримає папір із тією самою ТТН.
- **Отримувач офлайн:** відомий серверу гравець отримає папір під час наступного входу. Очікувана видача зберігається в даних світу.
- **Унікальні номери:** видані ТТН зберігаються у світі й не використовуються повторно.
- **Більші посилки:** до 27 різних типів предметів замість стандартних для Create 9.
- **Сортування в поїздах:** прикріпіть планшет до сховища поїзда (крім депо) і внесіть адреси в незакреслені рядки. Сховище без планшета є резервним для посилок, що не відповідають фільтрам; без нього посилка залишається на станції.
- **Відділення з мобом-оператором:** посадіть моба на сидіння Create, якщо в межах чотирьох блоків є сховище з інвентарем. Використайте папір із відповідною ТТН на мобі, щоб забрати посилку.
- **Спеціальні фільтри квакопортів:** `*+` пересилає посилки з буфера та доступних сховищ під квакопортом, зокрема сховищ Create, до налаштованої цілі, а `*-` приймає посилки без іншої відповідної адреси в мережі ланцюгових конвеєрів і вивантажує їх у нижнє сховище.
- **Мови:** англійська, російська та українська; мова інтерфейсу відповідає налаштуванню Minecraft.

### Вимоги та використання

Потрібні Minecraft 1.21.1, NeoForge 21.1.259 (або сумісна версія гілки 21.1) і Create 6.0.10. Встановіть однакову версію мода на клієнт і сервер.

1. Прикріпіть планшет до пакувальника та внесіть адреси в незакреслені рядки.
2. Натисніть кнопку пакувальника, виберіть адресу й натисніть **«Далі»**.
3. Вкажіть отримувача або залиште поле порожнім, а потім відправте посилку. Папір із ТТН отримають відправник і, якщо отримувача вказано, він. Офлайн-гравець отримає його під час входу.
4. Для сортування в поїзді прикріпіть планшети до сховищ із фільтрами; сховище без планшета буде резервним. Депо не враховуються.
5. Для відділення поставте сховище з інвентарем у межах чотирьох блоків від сидіння Create та посадіть на нього моба. Використайте відповідну ТТН на мобі.
6. Налаштуйте ціль квакопорта звичайним способом: `*+` пересилає посилки, `*-` приймає посилки без іншої відповідної адреси.
