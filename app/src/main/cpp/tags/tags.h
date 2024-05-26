#include "tpropertymap.h"
#include "tag.h"
#include "fileref.h"

namespace SoundSource {
    class AudioTagAccessor {
    public:
        AudioTagAccessor(int fileDescriptor, bool readonly);

        ~AudioTagAccessor();

        TagLib::Tag* tag();

        TagLib::FileRef* fileRef();

        long lastModified();

        void open();

        void close();

        bool isNull();

        bool isOpened();

    private:
        TagLib::FileRef *pfileRef;
        int fileDescriptor;
        bool readonly;

        void internalOpen(int fileDescriptor, bool readonly);
    };
}
