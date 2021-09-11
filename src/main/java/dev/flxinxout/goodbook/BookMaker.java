package dev.flxinxout.goodbook;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.bukkit.inventory.meta.BookMeta.Generation;

/**
 * <b>BookMaker</b>
 * <p>This Utility Class provides a great and fast Minecraft Custom Books Constructor for Minecraft Versions 1.8 to 1.16.5</p>
 * <p>If you want to use this class, just credit me it would be very nice</p>
 * @author flxinxout
 * @since September 2021
 * @version 1.2
 */
public final class BookMaker {

    private static String versionName;
    private static Field craftBookMetaField;
    private static Method chatSerializer;
    private static String NMS;
    private static final boolean canChatColorBungee;

    static {
        boolean garantee = true;
        try {
            ChatColor.BLACK.asBungee();

            versionName = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
            NMS = "net.minecraft.server." + versionName + ".";
            craftBookMetaField = Reflection.getField(Reflection.getClass("org.bukkit.craftbukkit." + versionName + ".inventory.CraftMetaBook"), "pages");
            Class<?> chatBase = Reflection.getClass(NMS + "IChatBaseComponent$ChatSerializer");
            chatSerializer = Reflection.getMethod(chatBase, "a", String.class);

        } catch (BookException e) {
            e.printStackTrace();
            garantee = false;
        }
        canChatColorBungee = garantee;
    }

    /**
     * @return a new constructor of a custom book
     */
    public static BookBuilder build() {
        return new BookBuilder();
    }

    public static void openBook(Player player, ItemStack book) {
        int slot = player.getInventory().getHeldItemSlot();
        ItemStack old = player.getInventory().getItem(slot);
        player.getInventory().setItem(slot, book);
        ByteBuf buf = Unpooled.buffer(256);
        buf.setByte(0, (byte) 0);
        buf.writerIndex(1);

        //get player handle
        Reflection.openBook(player, buf);
        player.getInventory().setItem(slot, old);
    }

    /**
     * This class represents a builder for the custom book to keep the principle of Design Pattern Builder
     *
     * @author flxinxout
     * @since September 2021
     * @version 2.0
     */
    public static final class BookBuilder {

        private final ItemStack book;
        private final BookMeta meta;

        /**
         * Constructor of a BookBuilder
         */
        public BookBuilder() {
            this.book = new ItemStack(Material.WRITTEN_BOOK);
            this.meta = (BookMeta) this.book.getItemMeta();
        }

        /**
         * Method that sets a page of the book
         * @param page the page
         * @return the current instance of the BookBuilder
         */
        public BookBuilder withPage(final BaseComponent[] page) {
            Reflection.setPage(page, meta, book);
            return this;
        }

        /**
         * Method that sets many pages of the book
         * @param pages the pages
         * @return the current instance of the BookBuilder
         */
        public BookBuilder withPages(final BaseComponent[]... pages) {
            for(BaseComponent[] page : pages) Reflection.setPage(page, meta, book);
            return this;
        }

        /**
         * Method that sets the title of the book
         * @param title the title
         * @throws IllegalArgumentException if the length of the title is > 32 characters
         *
         * @return the current instance of the BookBuilder
         */
        public BookBuilder withTitle(final String title) {
            if (title.length() > 32) {
                throw new IllegalArgumentException("The book title must be at most 32 characters");
            }
            Objects.requireNonNull(this.meta).setTitle(title);
            return this;
        }

        /**
         * Method that sets the author of the book
         * @param author the author
         * @return the current instance of the BookBuilder
         */
        public BookBuilder withAuthor(final String author) {
            Objects.requireNonNull(this.meta).setAuthor(author);
            return this;
        }

        /**
         * Method that sets the lore of the book
         * @param lore the lore
         * @return the current instance of the BookBuilder
         */
        public BookBuilder withLore(final String... lore) {
            Objects.requireNonNull(this.meta).setLore(new ArrayList<>(Arrays.asList(lore)));
            return this;
        }

        /**
         * Method that sets the lore of the book
         * @param lore the lore
         * @return the current instance of the BookBuilder
         */
        public BookBuilder withListLore(final List<String> lore) {
            Objects.requireNonNull(this.meta).setLore(lore);
            return this;
        }

        /**
         * Method that sets the book unbreakable
         * @param unbreakable the boolean for the invincibility
         * @return the current instance of the BookBuilder
         */
        public BookBuilder unbreakable(final boolean unbreakable) {
            Objects.requireNonNull(this.meta).setUnbreakable(unbreakable);
            return this;
        }

        /**
         * Method that sets the generation of the book
         *
         * @implNote <h3>Warning: putting null results in the original generation</h3>
         * @implNote <h3>Warning: only works for 1.10 to 1.16.5</h3>
         * @param generation the generation
         * @return the current instance of the BookBuilder
         */
        public BookBuilder withGeneration(final Generation generation) { // cannot be null -> original / only since 1.10
            Objects.requireNonNull(this.meta).setGeneration(generation);
            return this;
        }

        /**
         * Method that sets an ItemFlag for the book
         * @param flag the flag
         * @return the current instance of the BookBuilder
         */
        public BookBuilder withItemFlag(final ItemFlag flag) {
            Objects.requireNonNull(this.meta).addItemFlags(flag);
            return this;
        }

        /**
         * Method that sets many ItemFlag for the book
         * @param flags the flags
         * @return the current instance of the BookBuilder
         */
        public BookBuilder withItemFlags(final ItemFlag... flags) {
            for(ItemFlag flag : flags) {
                Objects.requireNonNull(this.meta).addItemFlags(flag);
            }
            return this;
        }

        /**
         * Method that remove all the ItemFlag of the book
         * @return the current instance of the BookBuilder
         */
        public BookBuilder removeItemFlags() {
            for(ItemFlag flag : ItemFlag.values()) {
                Objects.requireNonNull(this.meta).removeItemFlags(flag);
            }
            return this;
        }

        /**
         * Method that return the custom book with all the modifications added before
         * If the author is empty or the title if empty, or there is no page, the method set a title, an author and
         * an empty page
         * @return the ItemStack that represent the custom book
         */
        public ItemStack done() {
            if (!this.meta.hasAuthor()) {
                this.meta.setAuthor("");
            }
            if (!this.meta.hasTitle()) {
                this.meta.setTitle("");
            }
            if (!this.meta.hasPages()) {
                this.meta.addPage("");
            }
            this.book.setItemMeta(this.meta);
            return this.book;
        }
    }

    /**
     * This class represents a builder for a page of the custom book to keep the principle of Design Pattern Builder
     *
     * @author flxinxout
     * @since September 2021
     * @version 2.0
     */
    public static final class PageBook {

        private final List<BaseComponent> page = new ArrayList<>();

        /**
         * Method that create a simple page of the book with a simple String
         * @param component the text
         * @return a page of the custom book
         */
        public static BaseComponent[] of(final String component) {
            return new PageBook().with(component).done();
        }

        /**
         * Method that add a text to a page in construction
         * @param component the text
         * @return the current instance of the page
         */
        public PageBook with(final String component) {
            this.page.add(TextBook.of(component));
            return this;
        }

        /**
         * Method that add many texts to a page in construction
         * @param component the texts
         * @return the current instance of the page
         */
        public PageBook with(final String... component) {
            for(String s : component) {
                this.page.add(TextBook.of(s));
            }
            return this;
        }

        /**
         * Method that add a text to a page in construction
         * @see TextBook for the creation of the BaseComponent
         * @param component the text
         * @return the current instance of the page
         */
        public PageBook with(final BaseComponent component) {
            this.page.add(component);
            return this;
        }

        /**
         * Method that add many text to a page in construction
         * @see TextBook for the creation of the BaseComponents
         * @param component the texts
         * @return the current instance of the page
         */
        public PageBook with(final BaseComponent... component) {
            Collections.addAll(this.page, component);
            return this;
        }

        /**
         * @return the finished page of the future custom book
         */
        public BaseComponent[] done() {
            return this.page.toArray(new BaseComponent[0]);
        }
    }

    /**
     * This class represents a builder for a text of a page of the custom book to keep the principle of Design Pattern Builder
     *
     * @author flxinxout
     * @since September 2021
     * @version 2.0
     */
    public static final class TextBook {

        private String text = "";
        private HoverEvent hoverEvent = null;
        private ClickEvent clickEvent = null;
        private ChatColor color = ChatColor.BLACK;
        private ChatColor[] colorsStyle = null;

        /**
         * Method that add a hover event to the text
         * @param event the hover event
         * @return the current instance of the text in construction
         */
        public TextBook withHover(final HoverEvent event) {
            this.hoverEvent = event;
            return this;
        }

        /**
         * Method that add a click event to the text
         * @param event the click event
         * @return the current instance of the text in construction
         */
        public TextBook withClick(final ClickEvent event) {
            this.clickEvent = event;
            return this;
        }

        /**
         * Method that set the text
         * @param text the text
         * @return the current instance of the text in construction
         */
        public TextBook withText(final String text) {
            this.text = text;
            return this;
        }

        /**
         * Method that add a color to the text
         * @param color the color
         * @return the current instance of the text in construction
         */
        public TextBook withColor(final ChatColor color) {
            this.color = color;
            return this;
        }

        /**
         * Method that add styles to the text
         * @param style the styles
         * @return the current instance of the text in construction
         */
        public TextBook withStyle(final ChatColor... style) {
            this.colorsStyle = style;
            return this;
        }

        /**
         * Method that create a custom text with a simple String for simple texts
         * @param text the text
         * @return the custom text finished
         */
        public static BaseComponent of(final String text) {
            return new TextBook().withText(text).done();
        }

        /**
         * @return the custom text finished with all the styles added
         */
        public BaseComponent done() {
            final TextComponent component = new TextComponent(text);
            if(clickEvent != null) {
                component.setClickEvent(new ClickEvent(this.clickEvent.getAction(), this.clickEvent.getValue()));
            }
            if(hoverEvent != null) {
                component.setHoverEvent(new HoverEvent(this.hoverEvent.getAction(), this.hoverEvent.getValue()));
            }
            if(canChatColorBungee) {
                component.setColor(this.color.asBungee());
            } else {
                component.setColor(net.md_5.bungee.api.ChatColor.getByChar(this.color.getChar()));
            }

            if(this.colorsStyle != null) {
                for (ChatColor chatColor : this.colorsStyle) {
                    switch (chatColor) {
                        case BOLD:
                            component.setBold(true);
                            break;

                        case MAGIC:
                            component.setObfuscated(true);
                            break;

                        case ITALIC:
                            component.setItalic(true);
                            break;

                        case UNDERLINE:
                            component.setUnderlined(true);
                            break;

                        case STRIKETHROUGH:
                            component.setStrikethrough(true);
                            break;

                        default:
                            break;
                    }
                }
            }
            return component;
        }
    }

    /**
     * This class represents a custom exception class for the reflection
     *
     * @author flxinxout
     * @since September 2021
     * @version 2.0
     */
    private static final class BookException extends Exception {

        public BookException(final String message, final Throwable error) {
            super(message, error);
        }

    }

    /**
     * This class represents a small utility class for minecraft reflection.
     * This class is only useful for this project because there are many things not implemented
     *
     * @author flxinxout
     * @since September 2021
     * @version 2.0
     */
    private static final class Reflection {

        /**
         * Method that return a class by it's name
         * @param className the class name
         * @return the Class with this name
         * @throws BookException if the class is not found
         */
        public static Class<?> getClass(final String className) throws BookException {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new BookException(e.getMessage(), e);
            }
        }

        /**
         * Method that return a field of a class by it's name
         * @param clazz the class name
         * @param fieldName the field name
         * @return the Field of the class
         * @throws BookException if the field is not found
         */
        public static Field getField(final Class<?> clazz, final String fieldName) throws BookException {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                throw new BookException(e.getMessage(), e);
            }
        }

        /**
         * Method that return a method of a class by it's name and it's parameter types
         * @param clazz the class name
         * @param methodName the method name
         * @param typesParameters the types of the parameters
         * @return the Method of the class
         * @throws BookException if the method is not found
         */
        public static Method getMethod(final Class<?> clazz, final String methodName, final Class<?>... typesParameters) throws BookException {
            try {
                return clazz.getMethod(methodName, typesParameters);
            } catch (NoSuchMethodException e) {
                throw new BookException(e.getMessage(), e);
            }
        }

        /**
         * Method that invoke a method of a class
         * @param method the method name
         * @param params the parameters
         * @return the object returned by the method
         * @throws BookException if the invocation if not possible
         */
        public static Object invokeMethodForPages(final Method method, final Object... params) throws BookException {
            try {
                return method.invoke(null, params);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new BookException(e.getMessage(), e);
            }
        }
        /**
         * Method that get a constructor of a class
         * @param clazz the class name
         * @param params the parameters types
         * @return the constructor of the class
         * @throws BookException if the invocation if not possible
         */
        public static Constructor<?> getConstructor(final Class<?> clazz, final Class<?>... params) throws BookException {
            try {
                return clazz.getConstructor(params);
            } catch (NoSuchMethodException e) {
                throw new BookException(e.getMessage(), e);
            }
        }

        /**
         * Method that set a page to the book in turns of the minecraft version
         * @param page the page
         * @param meta the meta of the book
         * @param book the book
         */
        public static void setPage(final BaseComponent[] page, final BookMeta meta, final ItemStack book) {
            final String string = ComponentSerializer.toString(page);
            if(versionName.equalsIgnoreCase("v1_16_R3")) {

                Objects.requireNonNull(meta).spigot().addPage(new BaseComponent[][]{page});
                book.setItemMeta(meta);
            } else {

                try {
                    List<Object> p = (List<Object>) craftBookMetaField.get(meta);
                    p.add(invokeMethodForPages(chatSerializer, string));
                    book.setItemMeta(meta);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Method that open a book server side. The player will only see the book open and not the item in this hand
         * @param player the player
         * @param buf the buffer that will contain information
         */
        public static void openBook(Player player, ByteBuf buf) {
            try {

                Object nmsPlayer = player.getClass().getMethod("getHandle").invoke(player);
                Object connection = nmsPlayer.getClass().getField("playerConnection").get(nmsPlayer);

                if(versionName.equalsIgnoreCase("v1_13_r2")) {

                    // 1.13.2
                    Class<?> packetDataSerializer = getClass(NMS + "PacketDataSerializer");
                    Constructor<?> packetDataSerializerConstructor = getConstructor(packetDataSerializer, ByteBuf.class);
                    Class<?> packetPlayOutCustomPayload = getClass(NMS + "PacketPlayOutCustomPayload");
                    Class<?> minecraftKey = getClass(NMS + "MinecraftKey");
                    Constructor<?> constructorKey = getConstructor(minecraftKey, String.class);
                    Constructor<?> packetPlayOutCustomPayloadConstructor = getConstructor(packetPlayOutCustomPayload, minecraftKey, packetDataSerializer);
                    connection.getClass().getMethod("sendPacket", Class.forName(NMS + "Packet")).invoke(connection, packetPlayOutCustomPayloadConstructor.newInstance(constructorKey.newInstance("minecraft:book_open"), packetDataSerializerConstructor.newInstance(buf)));
                    return;
                }

                if(versionName.equalsIgnoreCase("versionName") || versionName.equalsIgnoreCase("v1_14_r1") || versionName.equalsIgnoreCase("v1_15_r1")) {
                    // 1.14.4 - 1.16.5
                    Class<?> packet = getClass(NMS + "Packet");
                    Class<?> enumHand = getClass(NMS + "EnumHand");
                    Object[] enumArray = enumHand.getEnumConstants();
                    Class<?> packetClass = getClass(NMS + "PacketPlayOutOpenBook");
                    Constructor<?> packetPlayOutOpenBook = getConstructor(packetClass, enumHand);
                    Object packetOpenBook = packetPlayOutOpenBook.newInstance(enumArray[0]);
                    connection.getClass().getMethod("sendPacket", packet).invoke(connection, packetOpenBook);
                    return;
                }

                if(versionName.equalsIgnoreCase("v1_8_r3") || versionName.equalsIgnoreCase("v1_9_r2") || versionName.equalsIgnoreCase("v1_10_r1") ||
                        versionName.equalsIgnoreCase("v1_11_r1") || versionName.equalsIgnoreCase("v1_12_r1")) {

                    // 1.8.9 - 1.9.4 - 1.10.2 - 1.11.2 - 1.12.2
                    Class<?> packetDataSerializer = getClass(NMS + "PacketDataSerializer");
                    Constructor<?> packetDataSerializerConstructor = getConstructor(packetDataSerializer, ByteBuf.class);
                    Class<?> packetPlayOutCustomPayload = getClass(NMS + "PacketPlayOutCustomPayload");
                    Constructor<?> packetPlayOutCustomPayloadConstructor = getConstructor(packetPlayOutCustomPayload, String.class, getClass(NMS + "PacketDataSerializer"));
                    connection.getClass().getMethod("sendPacket", getClass(NMS + "Packet")).invoke(connection, packetPlayOutCustomPayloadConstructor.newInstance("MC|BOpen", packetDataSerializerConstructor.newInstance(buf)));

                }

            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | NoSuchFieldException | ClassNotFoundException | InstantiationException | BookException e) {
                e.printStackTrace();
            }
        }
    }
}



















