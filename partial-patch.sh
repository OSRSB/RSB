target_file="~/Code/OsrsBot/src/main/java/net/runelite/rsb/wrappers/client_wrapper/RSClient.java"
lein run 2>&1 | while read -r line; do
    if echo "$line" | grep -q "return type void is not compatible with"; then
        type=$(echo "$line" | awk '{print $9}')
    fi
    if echo "$line" | grep -q "RSClient is not abstract and does not override abstract method"; then
        method=$(echo "$line" | awk '{print $13}')
        echo "public void $method { $method; }"
        sed -i '$ d'  "$target_file"
        echo "public void $method { $method; }" >> "$target_file"
        echo "}" >> "$target_file"
    fi
done

lein run 2>&1 | while read -r line; do
    if echo "$line" | grep -q "return type void is not compatible with"; then
        type=$(echo "$line" | awk '{print $8}')
        echo "public $type $method { $method; }"
        sed -i '$ d'  "$target_file"
        sed -i '$ d'  "$target_file"
        echo "public $type $method { return $method; }" >> "$target_file"
        echo "}" >> "$target_file"
    fi
    if echo "$line" | grep -q "RSClient is not abstract and does not override abstract method"; then
        method=$(echo "$line" | awk '{print $13}')

    fi
done



