package com.andresequeira.adapterception;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith({AdapterCeptionTest.TimingExtension.class})
public class AdapterCeptionTest {

    private static final int DEFAULT_SIZE = 10;

    @Mock
    RecyclerView recyclerView;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        new AdapterCeption<>();
    }

    private <VB extends AdapterCeption<?>> VB setUp(VB viewBinder) {
        viewBinder.onAttachedToRecyclerView(recyclerView);
        return viewBinder;
    }

    @Test
    public void test_0Level1_AdapterCeption() {
        final AdapterCeption<?> viewBinder = setUp(
                new _0Level1_AdapterCeption()
        );
        print(viewBinder);
    }

    @Test
    public void test_1Level1_AdapterCeption() {
        final EmptyAdapterCeption viewBinder = setUp(
                new _1Level1_AdapterCeption(0)
        );
        print(viewBinder);

        final EmptyAdapterCeption child = viewBinder.getChild(0);

        child.update(10);

        print(viewBinder);

        child.assertBind(8);
        viewBinder.rootBind(null, 8);
        viewBinder.assertsCalled();

        child.update(20);

        print(viewBinder);

        child.assertBind(18);
        viewBinder.rootBind(null, 18);
        viewBinder.assertsCalled();
    }

    @Test
    public void test_1Level2_AdapterCeption() {
        final AdapterCeption<?> viewBinder = setUp(
                new _1Level2_AdapterCeption(0, 2)
        );
        print(viewBinder);

        final EmptyAdapterCeption header = viewBinder.getChild("header");
        assertNotNull(header);
        header.update(1);

        print(viewBinder);

        final EmptyAdapterCeption tail = viewBinder.getChild("tail");
        assertNotNull(tail);
        tail.update(4);

        print(viewBinder);
    }

    @Test
    public void test_1Level2_0level1_AdapterCeption() {
        final AdapterCeption<?> viewBinder = setUp(
                new _1Level2_0level1_AdapterCeption()
        );
        print(viewBinder);
    }

    @Test
    public void test_1Level1_0level1_1Level1_AdapterCeption() {
        final AdapterCeption<?> viewBinder = setUp(
                new _1Level1_0level1_1Level1_AdapterCeption()
        );
        print(viewBinder);
    }

    @Test
    public void test_1Level1_0Level1_AdapterCeption() {
        final AdapterCeption<?> viewBinder = setUp(
                new _1Level1_0Level1_AdapterCeption(DEFAULT_SIZE, DEFAULT_SIZE)
        );
        print(viewBinder);
    }

    @Test
    public void test_0Level1_1Level1_AdapterCeption() {
        final AdapterCeption<?> viewBinder = setUp(
                new _0Level1_1Level1_AdapterCeption(0, 0)
        );
        print(viewBinder);
    }

    @Test
    public void test_Big_AdapterCeption() {
        final AdapterCeption<?> viewBinder = setUp(
                new Big_AdapterCeption()
        );
        print(viewBinder);
    }

    void print(AdapterCeption binder) {
        System.out.println(binder.getTreeInfo());
    }

    static class EmptyAdapterCeption extends AdapterCeption<Object> {

        int oldCount;
        int count;
        private int assertBindPosition = -1;

        public EmptyAdapterCeption() {
        }

        public EmptyAdapterCeption(int count) {
            this.count = count;
        }

        void update(int count) {
            oldCount = this.count;
            this.count = count;
            update();
        }

        @Override
        protected DiffUtil.Callback onUpdate() {
            return new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return oldCount;
                }

                @Override
                public int getNewListSize() {
                    return count;
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return false;
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return false;
                }
            };
        }

        @Override
        protected void dispatchUpdates(@NonNull DiffUtil.DiffResult diffResult, int offset) {

        }

        @Override
        protected int count() {
            return count;
        }

        @Override
        protected ViewProvider newViewProvider() {
            return new ViewProvider() {
                @Override
                protected Object newViewWrapper(@NonNull ViewGroup parent, int viewType) {
                    return null;
                }

                @Override
                protected View getView(Object o) {
                    return null;
                }
            };
        }

        @Override
        public void bind(@NonNull Object viewWrapper, int position) {
            assertEquals(assertBindPosition, position);
            assertBindPosition = -1;
        }

        public void assertBind(int assetBindPosition) {
            this.assertBindPosition = assetBindPosition;
        }

        void assertsCalled() {
            apply(new Action() {
                @Override
                public void apply(AdapterCeption<?> vb) {
                    if (vb instanceof EmptyAdapterCeption) {
                        if (((EmptyAdapterCeption) vb).assertBindPosition != -1) {
                            throw new RuntimeException(EmptyAdapterCeption.this + ": bind not called for position: " + assertBindPosition);
                        }
                    }

                }
            });
        }
    }

    static class _0Level1_AdapterCeption extends EmptyAdapterCeption {

        public _0Level1_AdapterCeption() {
            super(DEFAULT_SIZE);
        }

        public _0Level1_AdapterCeption(int count) {
            super(count);
        }
    }

    static class _1Level1_AdapterCeption extends EmptyAdapterCeption {

        _1Level1_AdapterCeption(int count) {
            add(new _0Level1_AdapterCeption(count));
        }
    }

    static class _1Level2_AdapterCeption extends EmptyAdapterCeption {

        _1Level2_AdapterCeption(int c1, int c2) {
            add(
                    new _0Level1_AdapterCeption(c1).setTag("header"),
                    new _0Level1_AdapterCeption(c2).setTag("tail")
            );
        }
    }

    static class _1Level2_0level1_AdapterCeption extends AdapterCeption {

        _1Level2_0level1_AdapterCeption() {
            add(
                    new _0Level1_AdapterCeption(),
                    new _0Level1_AdapterCeption()
            );
        }

        @Override
        protected int count() {
            return DEFAULT_SIZE;
        }
    }

    static class _1Level1_0level1_1Level1_AdapterCeption extends AdapterCeption {

        _1Level1_0level1_1Level1_AdapterCeption() {
            add(
                    new _0Level1_AdapterCeption(),
                    new _0Level1_AdapterCeption()
            );
            setRelativePosition(1);
        }

        @Override
        protected int count() {
            return DEFAULT_SIZE;
        }
    }

    static class _1Level1_0Level1_AdapterCeption extends EmptyAdapterCeption {

        public _1Level1_0Level1_AdapterCeption(int c1, int c0) {
            super(c0);
            add(
                    new _0Level1_AdapterCeption(c1)
            );
        }

        @Override
        protected int count() {
            return DEFAULT_SIZE;
        }
    }

    static class _0Level1_1Level1_AdapterCeption extends _1Level1_0Level1_AdapterCeption {

        _0Level1_1Level1_AdapterCeption(int c0, int c1) {
            super(c1, c0);
            setRelativePosition(POSITION_FIRST);
        }
    }

    static class Big_AdapterCeption extends EmptyAdapterCeption {
        Big_AdapterCeption() {
            super(DEFAULT_SIZE);
            add(
                    new _0Level1_AdapterCeption().add(
                            new _0Level1_AdapterCeption(),
                            new _0Level1_AdapterCeption().add(
                                    new _0Level1_AdapterCeption()
                            ).setRelativePosition(POSITION_LAST)
                    ).setRelativePosition(1),
                    new _0Level1_AdapterCeption(),
                    new _0Level1_AdapterCeption().add(
                            new _0Level1_AdapterCeption().add(
                                    new _0Level1_AdapterCeption()
                            ),
                            new _0Level1_AdapterCeption(),
                            new _0Level1_AdapterCeption(),
                            new _0Level1_AdapterCeption().add(
                                    new _0Level1_AdapterCeption().add(
                                            new _0Level1_AdapterCeption()
                                    )
                            )
                    ).setRelativePosition(POSITION_FIRST),
                    new _0Level1_AdapterCeption().add(
                            new _0Level1_AdapterCeption().add(
                                    new _0Level1_AdapterCeption().add(
                                            new _0Level1_AdapterCeption().add(
                                                    new _0Level1_AdapterCeption()
                                            ).setRelativePosition(POSITION_FIRST)
                                    )
                            ),
                            new _0Level1_AdapterCeption().add(
                                    new _0Level1_AdapterCeption()
                            ).setRelativePosition(POSITION_FIRST)
                    )
            );
            setRelativePosition(2);
        }
    }

    public static class TimingExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

        private static final Logger logger = Logger.getLogger(TimingExtension.class.getName());

        private static final String START_TIME = "start time";

        @Override
        public void beforeTestExecution(ExtensionContext context) throws Exception {
            getStore(context).put(START_TIME, System.currentTimeMillis());
        }

        @Override
        public void afterTestExecution(ExtensionContext context) throws Exception {
            final long end = System.currentTimeMillis();
            final Method testMethod = context.getRequiredTestMethod();
            long startTime = getStore(context).remove(START_TIME, long.class);
            final long duration = end - startTime;

            logger.info(new Supplier<String>() {
                @Override
                public String get() {
                    return String.format("Method [%s] took %s ms.", testMethod.getName(), duration);
                }
            });
        }

        private ExtensionContext.Store getStore(ExtensionContext context) {
            return context.getStore(ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod()));
        }

    }

}
