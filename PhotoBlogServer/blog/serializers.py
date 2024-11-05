from blog.models import Post
from rest_framework import serializers
from django.contrib.auth.models import User

class PostSerializer(serializers.HyperlinkedModelSerializer):
    author = serializers.PrimaryKeyRelatedField(queryset=User.objects.all())
    
    class Meta:
        model = Post
        fields = ('author', 'title', 'text', 'image')
    
    def create(self, validated_data):
        print("Received Data:", validated_data)
        
        post = Post.objects.create(**validated_data)
        
        return post